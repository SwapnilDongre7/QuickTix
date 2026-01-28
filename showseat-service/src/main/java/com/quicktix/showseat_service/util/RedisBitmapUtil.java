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
    private static final String SEAT_LOCK_PREFIX = "SEAT_LOCK:";          // STRING (SETNX)
    private static final String SEAT_LOCK_META_PREFIX = "SEAT_LOCK_META:"; // HASH
    private static final String SEAT_MAPPING_PREFIX = "SEAT_MAP:";

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
                redisTemplate.opsForValue().getBit(SEAT_AVAILABILITY_PREFIX + showId, seatIndex)
        );
    }

    public boolean isSeatLocked(String showId, int seatIndex) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(lockKey(showId, seatIndex))
        );
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

    /* ===================== VALIDATION ===================== */

    public Map<Integer, Boolean> validateSeatsLockedByUser(
            String showId,
            List<Integer> seatIndices,
            Long userId,
            String sessionId
    ) {
        Map<Integer, Boolean> result = new HashMap<>();

        for (Integer index : seatIndices) {
            boolean locked = isSeatLockedByUser(showId, index, userId, sessionId);
            result.put(index, locked);

            if (!locked) {
                log.warn(
                        "Seat {} NOT locked by user {} session {}",
                        index, userId, sessionId
                );
            }
        }
        return result;
    }

    /* ===================== LOCK ===================== */

    public List<Integer> lockSeats(
            String showId,
            List<Integer> seatIndices,
            Long userId,
            String sessionId,
            long ttlSeconds
    ) {
        List<Integer> locked = new ArrayList<>();

        for (Integer index : seatIndices) {

            if (isSeatBooked(showId, index)) {
                log.warn("Seat {} already booked for show {}", index, showId);
                continue;
            }

            String lockKey = lockKey(showId, index);
            String metaKey = metaKey(showId, index);

            // 🔒 ATOMIC LOCK (SETNX)
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", ttlSeconds, TimeUnit.SECONDS);

            if (!Boolean.TRUE.equals(acquired)) {
                log.warn("Seat {} already locked for show {}", index, showId);
                continue;
            }

            Map<String, String> meta = new HashMap<>();
            meta.put("userId", userId.toString());
            meta.put("sessionId", sessionId);
            meta.put("lockedAt", String.valueOf(System.currentTimeMillis()));

            redisTemplate.opsForHash().putAll(metaKey, meta);
            redisTemplate.expire(metaKey, ttlSeconds, TimeUnit.SECONDS);

            locked.add(index);
            log.debug("Locked seat {} for show {} by user {}", index, showId, userId);
        }

        log.info("Locked {} seats for show {} by user {}", locked.size(), showId, userId);
        return locked;
    }

    /* ===================== UNLOCK ===================== */

    public int unlockSeats(
            String showId,
            List<Integer> seatIndices,
            Long userId,
            String sessionId
    ) {
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
                        e -> e.getValue().toString()
                ));

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

        if (locks != null) redisTemplate.delete(locks);
        if (metas != null) redisTemplate.delete(metas);

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
//
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class RedisBitmapUtil {
//
//    private final RedisTemplate<String, Object> redisTemplate;
//
//    private static final String SEAT_AVAILABILITY_PREFIX = "SEAT_AVAIL:";
//    private static final String SEAT_LOCK_PREFIX = "SEAT_LOCK:";
//    private static final String SEAT_MAPPING_PREFIX = "SEAT_MAP:";
//
//    public void initializeSeatAvailability(String showId, int totalSeats) {
//        String availKey = SEAT_AVAILABILITY_PREFIX + showId;
//
//        for (int i = 0; i < totalSeats; i++) {
//            redisTemplate.opsForValue().setBit(availKey, i, false);
//        }
//
//        log.info("Initialized {} seats for show {}", totalSeats, showId);
//    }
//
//    public boolean isSeatBooked(String showId, int seatIndex) {
//        return Boolean.TRUE.equals(redisTemplate.opsForValue()
//                .getBit(SEAT_AVAILABILITY_PREFIX + showId, seatIndex));
//    }
//
//    public boolean isSeatLocked(String showId, int seatIndex) {
//        String lockKey = SEAT_LOCK_PREFIX + showId + ":" + seatIndex;
//        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
//    }
//
//    public boolean isSeatAvailable(String showId, int seatIndex) {
//        return !isSeatBooked(showId, seatIndex) && !isSeatLocked(showId, seatIndex);
//    }
//
//    public boolean isSeatLockedByUser(String showId, int seatIndex, Long userId, String sessionId) {
//        String lockKey = SEAT_LOCK_PREFIX + showId + ":" + seatIndex;
//        
//        if (!Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
//            return false;
//        }
//
//        Map<Object, Object> lockData = redisTemplate.opsForHash().entries(lockKey);
//        
//        if (lockData.isEmpty()) {
//            return false;
//        }
//
//        String storedUserId = (String) lockData.get("userId");
//        String storedSessionId = (String) lockData.get("sessionId");
//
//        return userId.toString().equals(storedUserId) && sessionId.equals(storedSessionId);
//    }
//
//    public Map<Integer, Boolean> validateSeatsLockedByUser(String showId, List<Integer> seatIndices, 
//                                                            Long userId, String sessionId) {
//        Map<Integer, Boolean> validationResults = new HashMap<>();
//
//        for (Integer index : seatIndices) {
//            boolean isLockedByUser = isSeatLockedByUser(showId, index, userId, sessionId);
//            validationResults.put(index, isLockedByUser);
//            
//            if (!isLockedByUser) {
//                log.warn("Seat index {} is NOT locked by user {} session {}", index, userId, sessionId);
//            }
//        }
//
//        return validationResults;
//    }
//
//    public List<Integer> lockSeats(String showId, List<Integer> seatIndices, Long userId,
//                                    String sessionId, long ttlSeconds) {
//        List<Integer> locked = new ArrayList<>();
//
//        for (Integer index : seatIndices) {
//            if (!isSeatAvailable(showId, index)) {
//                log.warn("Seat index {} for show {} is not available", index, showId);
//                continue;
//            }
//
//            String lockKey = SEAT_LOCK_PREFIX + showId + ":" + index;
//            
//            Map<String, String> lockData = new HashMap<>();
//            lockData.put("userId", userId.toString());
//            lockData.put("sessionId", sessionId);
//            lockData.put("lockedAt", String.valueOf(System.currentTimeMillis()));
//            lockData.put("showId", showId);
//            lockData.put("seatIndex", index.toString());
//            
//            redisTemplate.opsForHash().putAll(lockKey, lockData);
//            redisTemplate.expire(lockKey, ttlSeconds, TimeUnit.SECONDS);
//
//            locked.add(index);
//            log.debug("Locked seat {} for show {} by user {} (TTL: {}s)", index, showId, userId, ttlSeconds);
//        }
//
//        log.info("Locked {} seats for show {} by user {}", locked.size(), showId, userId);
//        return locked;
//    }
//
//    public void unlockSeats(String showId, List<Integer> seatIndices, Long userId, String sessionId) {
//        int unlockedCount = 0;
//
//        for (Integer index : seatIndices) {
//            String lockKey = SEAT_LOCK_PREFIX + showId + ":" + index;
//            
//            if (!Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
//                log.warn("No lock found for seat index {} in show {}", index, showId);
//                continue;
//            }
//
//            Map<Object, Object> lockData = redisTemplate.opsForHash().entries(lockKey);
//
//            if (lockData.isEmpty()) {
//                log.warn("No lock data found for seat index {} in show {}", index, showId);
//                continue;
//            }
//
//            String storedUserId = (String) lockData.get("userId");
//            String storedSessionId = (String) lockData.get("sessionId");
//
//            if (!userId.toString().equals(storedUserId)) {
//                log.warn("User {} attempted to unlock seat {} owned by user {}", userId, index, storedUserId);
//                continue;
//            }
//
//            if (!sessionId.equals(storedSessionId)) {
//                log.warn("Session {} attempted to unlock seat {} owned by session {}", sessionId, index, storedSessionId);
//                continue;
//            }
//
//            redisTemplate.delete(lockKey);
//            unlockedCount++;
//            log.debug("Unlocked seat {} for show {}", index, showId);
//        }
//
//        log.info("Unlocked {} seats for show {} by user {}", unlockedCount, showId, userId);
//    }
//
//    public void confirmBooking(String showId, List<Integer> seatIndices, Long userId, String sessionId) {
//        String availKey = SEAT_AVAILABILITY_PREFIX + showId;
//        int confirmedCount = 0;
//
//        for (Integer index : seatIndices) {
//            String lockKey = SEAT_LOCK_PREFIX + showId + ":" + index;
//            
//            if (!Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
//                log.error("CRITICAL: No lock found for seat index {} in show {} during confirm", index, showId);
//                continue;
//            }
//
//            Map<Object, Object> lockData = redisTemplate.opsForHash().entries(lockKey);
//
//            if (lockData.isEmpty()) {
//                log.error("CRITICAL: No lock data found for seat index {} in show {} during confirm", index, showId);
//                continue;
//            }
//
//            String storedUserId = (String) lockData.get("userId");
//            String storedSessionId = (String) lockData.get("sessionId");
//
//            if (!userId.toString().equals(storedUserId)) {
//                log.error("CRITICAL: User {} attempted to confirm seat {} owned by user {}", userId, index, storedUserId);
//                continue;
//            }
//
//            if (!sessionId.equals(storedSessionId)) {
//                log.error("CRITICAL: Session {} attempted to confirm seat {} owned by session {}", sessionId, index, storedSessionId);
//                continue;
//            }
//
//            redisTemplate.opsForValue().setBit(availKey, index, true);
//            redisTemplate.delete(lockKey);
//            confirmedCount++;
//            log.debug("Confirmed seat {} for show {}", index, showId);
//        }
//
//        log.info("Confirmed booking for {} seats in show {} by user {}", confirmedCount, showId, userId);
//    }
//
//    public void storeSeatMapping(String showId, Map<String, Integer> seatToIndex) {
//        String key = SEAT_MAPPING_PREFIX + showId;
//        Map<String, String> mappingAsString = seatToIndex.entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
//        
//        redisTemplate.opsForHash().putAll(key, mappingAsString);
//        log.info("Stored seat mapping for show {}: {} seats", showId, seatToIndex.size());
//    }
//
//    public Integer getSeatIndex(String showId, String seatNumber) {
//        Object val = redisTemplate.opsForHash().get(SEAT_MAPPING_PREFIX + showId, seatNumber);
//        return val != null ? Integer.parseInt(val.toString()) : null;
//    }
//
//    public String getSeatNumber(String showId, int seatIndex) {
//        Map<Object, Object> map = redisTemplate.opsForHash().entries(SEAT_MAPPING_PREFIX + showId);
//        for (Map.Entry<Object, Object> e : map.entrySet()) {
//            if (Integer.parseInt(e.getValue().toString()) == seatIndex) {
//                return e.getKey().toString();
//            }
//        }
//        return null;
//    }
//
//    public void clearLocksOnly(String showId) {
//        String lockPattern = SEAT_LOCK_PREFIX + showId + ":*";
//        Set<String> lockKeys = redisTemplate.keys(lockPattern);
//        
//        if (lockKeys != null && !lockKeys.isEmpty()) {
//            redisTemplate.delete(lockKeys);
//            log.info("Cleared {} lock keys for show {}", lockKeys.size(), showId);
//        }
//    }
//
//    public void clearShowData(String showId) {
//        redisTemplate.delete(SEAT_AVAILABILITY_PREFIX + showId);
//        redisTemplate.delete(SEAT_MAPPING_PREFIX + showId);
//        
//        String lockPattern = SEAT_LOCK_PREFIX + showId + ":*";
//        Set<String> lockKeys = redisTemplate.keys(lockPattern);
//        if (lockKeys != null && !lockKeys.isEmpty()) {
//            redisTemplate.delete(lockKeys);
//        }
//        
//        log.info("Cleared all seat data for show {}", showId);
//    }
//
//    public List<Integer> getAvailableSeats(String showId, int totalSeats) {
//        List<Integer> available = new ArrayList<>();
//        for (int i = 0; i < totalSeats; i++) {
//            if (isSeatAvailable(showId, i)) {
//                available.add(i);
//            }
//        }
//        return available;
//    }
//
//    public long getAvailableSeatsCount(String showId, int totalSeats) {
//        long count = 0;
//        for (int i = 0; i < totalSeats; i++) {
//            if (isSeatAvailable(showId, i)) {
//                count++;
//            }
//        }
//        return count;
//    }
//
//    public List<String> getExpiredLocks(String showId, int totalSeats) {
//        List<String> expiredSeats = new ArrayList<>();
//        
//        for (int i = 0; i < totalSeats; i++) {
//            String lockKey = SEAT_LOCK_PREFIX + showId + ":" + i;
//            
//            if (!Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
//                continue;
//            }
//            
//            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
//            
//            if (ttl != null && ttl <= 0) {
//                String seatNo = getSeatNumber(showId, i);
//                if (seatNo != null) {
//                    expiredSeats.add(seatNo);
//                }
//                redisTemplate.delete(lockKey);
//            }
//        }
//        
//        if (!expiredSeats.isEmpty()) {
//            log.info("Cleaned up {} expired locks for show {}", expiredSeats.size(), showId);
//        }
//        
//        return expiredSeats;
//    }
//}