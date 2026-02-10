package com.quicktix.showseat_service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisBitmapUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SEAT_AVAILABILITY_PREFIX = "SEAT_AVAIL:";
    private static final String SEAT_LOCK_PREFIX = "SEAT_LOCK:"; // STRING (SETNX)
    private static final String SEAT_LOCK_META_PREFIX = "SEAT_LOCK_META:"; // HASH
    private static final String SEAT_MAPPING_PREFIX = "SEAT_MAP:";
    private static final String BOOKING_PROCESSED_PREFIX = "BOOKING_PROCESSED:";

    /* ===================== IDEMPOTENCY ===================== */

    public boolean isBookingProcessed(String bookingId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BOOKING_PROCESSED_PREFIX + bookingId));
    }

    public void markBookingProcessed(String bookingId) {
        redisTemplate.opsForValue().set(BOOKING_PROCESSED_PREFIX + bookingId, "TRUE", 24, TimeUnit.HOURS);
    }

    /* ===================== INIT ===================== */

    public void initializeSeatAvailability(String showId, int totalSeats) {
        String key = SEAT_AVAILABILITY_PREFIX + showId;
        for (int i = 0; i < totalSeats; i++) {
            redisTemplate.opsForValue().setBit(key, i, false);
        }
        log.info("Initialized {} seats for show {}", totalSeats, showId);
    }

    /* ===================== STATE ===================== */

    public boolean isSeatBooked(String showId, int seatIndex) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().getBit(SEAT_AVAILABILITY_PREFIX + showId, seatIndex));
    }

    public boolean isSeatLocked(String showId, int seatIndex) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(lockKey(showId, seatIndex)));
    }

    public boolean isSeatAvailable(String showId, int seatIndex) {
        return !isSeatBooked(showId, seatIndex) && !isSeatLocked(showId, seatIndex);
    }

    public boolean isSeatLockedByUser(String showId, int seatIndex, Long userId, String sessionId) {
        String lockKey = lockKey(showId, seatIndex);
        String metaKey = metaKey(showId, seatIndex);

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            return false;
        }

        Map<Object, Object> meta = redisTemplate.opsForHash().entries(metaKey);
        if (meta.isEmpty()) {
            return false;
        }

        return userId.toString().equals(meta.get("userId"))
                && sessionId.equals(meta.get("sessionId"));
    }

    /**
     * Optimized method to get seat status in bulk using bitmasks.
     * Each seat takes 2 bits: 00=AVAIL, 01=LOCKED, 10=BOOKED, 11=BLOCKED
     */
    public byte[] getSeatStatusBitmask(String showId, int totalSeats) {
        byte[] bitmask = new byte[(totalSeats * 2 + 7) / 8];

        for (int i = 0; i < totalSeats; i++) {
            int status = 0; // AVAILABLE
            if (isSeatBooked(showId, i)) {
                status = 2; // BOOKED (10 in binary)
            } else if (isSeatLocked(showId, i)) {
                status = 1; // LOCKED (01 in binary)
            }

            int byteIndex = (i * 2) / 8;
            int bitOffset = (i * 2) % 8;
            bitmask[byteIndex] |= (byte) (status << bitOffset);
        }

        return bitmask;
    }

    /* ===================== VALIDATION ===================== */

    public Map<Integer, Boolean> validateSeatsLockedByUser(
            String showId,
            List<Integer> seatIndices,
            Long userId,
            String sessionId) {
        Map<Integer, Boolean> result = new HashMap<>();

        for (Integer index : seatIndices) {
            boolean locked = isSeatLockedByUser(showId, index, userId, sessionId);
            result.put(index, locked);

            if (!locked) {
                log.warn(
                        "Seat {} NOT locked by user {} session {}",
                        index, userId, sessionId);
            }
        }
        return result;
    }

    /* ===================== LOCK ===================== */

    /**
     * Atomically lock multiple seats using a Lua script.
     * This ensures "all-or-nothing" locking - if ANY seat is already locked or
     * booked,
     * the entire operation fails and no seats are locked.
     * 
     * @return List of locked seat indices (all requested seats if successful, empty
     *         if failed)
     * @throws com.quicktix.showseat_service.exception.SeatAlreadyLockedException if
     *                                                                            any
     *                                                                            seat
     *                                                                            is
     *                                                                            already
     *                                                                            locked
     * @throws com.quicktix.showseat_service.exception.SeatAlreadyBookedException if
     *                                                                            any
     *                                                                            seat
     *                                                                            is
     *                                                                            already
     *                                                                            booked
     */
    public List<Integer> lockSeatsAtomic(
            String showId,
            List<Integer> seatIndices,
            Long userId,
            String sessionId,
            long ttlSeconds) {

        if (seatIndices == null || seatIndices.isEmpty()) {
            log.warn("lockSeatsAtomic called with empty seat indices");
            return new ArrayList<>();
        }

        // Validate seat indices - must be non-negative integers
        for (Integer index : seatIndices) {
            if (index == null || index < 0) {
                log.error("Invalid seat index: {} for show {}", index, showId);
                throw new IllegalArgumentException("Invalid seat index: " + index);
            }
        }

        // Validate TTL
        if (ttlSeconds <= 0) {
            log.error("Invalid TTL: {} for show {}", ttlSeconds, showId);
            throw new IllegalArgumentException("TTL must be positive: " + ttlSeconds);
        }

        log.debug("lockSeatsAtomic: showId={}, indices={}, userId={}, ttl={}",
                showId, seatIndices, userId, ttlSeconds);

        // First, check if any seats are already booked (in the bitmap)
        for (Integer index : seatIndices) {
            if (isSeatBooked(showId, index)) {
                String seatNo = getSeatNumber(showId, index);
                log.warn("Seat {} (index {}) already booked for show {}", seatNo, index, showId);
                throw new com.quicktix.showseat_service.exception.SeatAlreadyBookedException(
                        "Seat " + (seatNo != null ? seatNo : index) + " is already booked");
            }
        }

        // Lua script for atomic multi-seat locking
        // ARGV[1]: TTL in seconds
        // ARGV[2]: lock value ("1")
        String luaScript = "local ttl = tonumber(ARGV[1]) " +
                "local lockValue = ARGV[2] " +
                "for i, key in ipairs(KEYS) do " +
                "  if redis.call('EXISTS', key) == 1 then " +
                "    return 0 " +
                "  end " +
                "end " +
                "for i, key in ipairs(KEYS) do " +
                "  redis.call('SET', key, lockValue, 'EX', ttl) " +
                "end " +
                "return 1";

        // Build lock keys
        List<String> lockKeys = new ArrayList<>();
        for (Integer index : seatIndices) {
            lockKeys.add(lockKey(showId, index));
        }

        log.debug("Executing Lua script with keys={}, ttl={}", lockKeys, ttlSeconds);

        try {
            // Use StringRedisTemplate for script execution to ensure arguments are not
            // JSON-quoted.
            // This is necessary because the default redisTemplate uses
            // GenericJackson2JsonRedisSerializer,
            // which would quote the TTL and lock value, causing Lua's tonumber() to fail.
            org.springframework.data.redis.core.StringRedisTemplate stringTemplate = new org.springframework.data.redis.core.StringRedisTemplate(
                    redisTemplate.getConnectionFactory());

            Long result = stringTemplate.execute(
                    new org.springframework.data.redis.core.script.DefaultRedisScript<>(luaScript, Long.class),
                    lockKeys,
                    String.valueOf(ttlSeconds),
                    "1");

            log.debug("Lua script result: {}", result);

            if (result == null || result == 0) {
                // Find which seat is already locked for better error message
                for (Integer index : seatIndices) {
                    if (isSeatLocked(showId, index)) {
                        String seatNo = getSeatNumber(showId, index);
                        log.warn("Seat {} (index {}) already locked for show {}", seatNo, index, showId);
                        throw new com.quicktix.showseat_service.exception.SeatAlreadyLockedException(
                                "One or more seats are already locked");
                    }
                }
                throw new com.quicktix.showseat_service.exception.SeatAlreadyLockedException(
                        "Failed to acquire locks for requested seats");
            }

            // Store metadata for each locked seat
            for (Integer index : seatIndices) {
                String metaKey = metaKey(showId, index);
                Map<String, String> meta = new HashMap<>();
                meta.put("userId", userId.toString());
                meta.put("sessionId", sessionId);
                meta.put("lockedAt", String.valueOf(System.currentTimeMillis()));
                redisTemplate.opsForHash().putAll(metaKey, meta);
                redisTemplate.expire(metaKey, ttlSeconds, TimeUnit.SECONDS);
            }

            log.info("Atomically locked {} seats for show {} by user {}", seatIndices.size(), showId, userId);
            return new ArrayList<>(seatIndices);

        } catch (org.springframework.data.redis.RedisSystemException e) {
            log.error("CRITICAL: Redis system error during seat locking for show {}: {}", showId, e.getMessage(), e);
            throw new RuntimeException("LOCK_OPERATION_FAILED: Redis system error: " + e.getMessage(), e);
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("CRITICAL: Redis data access error during seat locking for show {}: {}", showId, e.getMessage(),
                    e);
            throw new RuntimeException("LOCK_OPERATION_FAILED: Redis data access error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("CRITICAL: Unexpected error during seat locking for show {}: {}", showId, e.getMessage(), e);
            throw new RuntimeException("LOCK_OPERATION_FAILED: Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * @deprecated Use lockSeatsAtomic for production-grade atomic locking
     */
    @Deprecated
    public List<Integer> lockSeats(
            String showId,
            List<Integer> seatIndices,
            Long userId,
            String sessionId,
            long ttlSeconds) {
        // Delegate to atomic implementation
        return lockSeatsAtomic(showId, seatIndices, userId, sessionId, ttlSeconds);
    }

    /* ===================== UNLOCK ===================== */

    public int unlockSeats(
            String showId,
            List<Integer> seatIndices,
            Long userId,
            String sessionId) {
        int unlocked = 0;

        for (Integer index : seatIndices) {
            if (!isSeatLockedByUser(showId, index, userId, sessionId)) {
                continue;
            }

            redisTemplate.delete(lockKey(showId, index));
            redisTemplate.delete(metaKey(showId, index));
            unlocked++;
        }

        return unlocked;
    }

    /* ===================== CONFIRM ===================== */

    public void confirmBooking(String showId, List<Integer> seatIndices, Long userId, String sessionId) {
        String availKey = SEAT_AVAILABILITY_PREFIX + showId;
        int confirmed = 0;

        for (Integer index : seatIndices) {
            if (!isSeatLockedByUser(showId, index, userId, sessionId)) {
                log.error("Seat {} not locked by user {} during confirm", index, userId);
                continue;
            }

            redisTemplate.opsForValue().setBit(availKey, index, true);
            redisTemplate.delete(lockKey(showId, index));
            redisTemplate.delete(metaKey(showId, index));
            confirmed++;
        }

        log.info("Confirmed booking for {} seats in show {}", confirmed, showId);
    }

    /* ===================== MAPPING ===================== */

    public void storeSeatMapping(String showId, Map<String, Integer> seatToIndex) {
        String key = SEAT_MAPPING_PREFIX + showId;
        Map<String, String> map = seatToIndex.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toString()));

        redisTemplate.opsForHash().putAll(key, map);
        log.info("Stored seat mapping for show {} ({} seats)", showId, seatToIndex.size());
    }

    public Integer getSeatIndex(String showId, String seatNumber) {
        Object val = redisTemplate.opsForHash().get(SEAT_MAPPING_PREFIX + showId, seatNumber);
        return val != null ? Integer.parseInt(val.toString()) : null;
    }

    public String getSeatNumber(String showId, int seatIndex) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(SEAT_MAPPING_PREFIX + showId);
        for (Map.Entry<Object, Object> e : map.entrySet()) {
            if (Integer.parseInt(e.getValue().toString()) == seatIndex) {
                return e.getKey().toString();
            }
        }
        return null;
    }

    /* ===================== CLEANUP ===================== */

    public void clearLocksOnly(String showId) {
        Set<String> locks = redisTemplate.keys(SEAT_LOCK_PREFIX + showId + ":*");
        Set<String> metas = redisTemplate.keys(SEAT_LOCK_META_PREFIX + showId + ":*");

        if (locks != null && !locks.isEmpty())
            redisTemplate.delete(locks);
        if (metas != null && !metas.isEmpty())
            redisTemplate.delete(metas);

        log.info("Cleared all locks for show {}", showId);
    }

    public void clearShowData(String showId) {
        redisTemplate.delete(SEAT_AVAILABILITY_PREFIX + showId);
        redisTemplate.delete(SEAT_MAPPING_PREFIX + showId);
        clearLocksOnly(showId);
        log.info("Cleared all seat data for show {}", showId);
    }

    /* ===================== HELPERS ===================== */

    private String lockKey(String showId, int index) {
        return SEAT_LOCK_PREFIX + showId + ":" + index;
    }

    private String metaKey(String showId, int index) {
        return SEAT_LOCK_META_PREFIX + showId + ":" + index;
    }
}