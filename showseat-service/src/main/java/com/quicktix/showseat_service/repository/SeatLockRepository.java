package com.quicktix.showseat_service.repository;

import java.time.Duration;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.quicktix.showseat_service.model.redis.SeatLock;

import lombok.RequiredArgsConstructor;

//@Repository
//@RequiredArgsConstructor
//public class SeatLockRepository {
//
//    private final RedisTemplate<String, Object> redisTemplate;
//    private static final String LOCK_META_PREFIX = "LOCK_META:";
//
//    public void saveLockMetadata(SeatLock seatLock, long ttlSeconds) {
//        String key = LOCK_META_PREFIX + seatLock.getShowId() + ":" + 
//                     getSeatIndexFromSeatNo(seatLock.getShowId(), seatLock.getSeatNo());
//        
//        Map<String, String> metadata = Map.of(
//            "showId", seatLock.getShowId(),
//            "seatNo", seatLock.getSeatNo(),
//            "userId", String.valueOf(seatLock.getUserId()),
//            "sessionId", seatLock.getSessionId(),
//            "lockedAt", seatLock.getLockedAt().toString(),
//            "expiresAt", seatLock.getExpiresAt().toString()
//        );
//        
//        redisTemplate.opsForHash().putAll(key, metadata);
//        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
//    }
//
//    public Map<Object, Object> getLockMetadata(String showId, String seatNo) {
//        String key = LOCK_META_PREFIX + showId + ":" + getSeatIndexFromSeatNo(showId, seatNo);
//        return redisTemplate.opsForHash().entries(key);
//    }
//
//    public void deleteLockMetadata(String showId, String seatNo) {
//        String key = LOCK_META_PREFIX + showId + ":" + getSeatIndexFromSeatNo(showId, seatNo);
//        redisTemplate.delete(key);
//    }
//
//    public boolean hasLock(String showId, String seatNo) {
//        String key = LOCK_META_PREFIX + showId + ":" + getSeatIndexFromSeatNo(showId, seatNo);
//        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
//    }
//
//    private Integer getSeatIndexFromSeatNo(String showId, String seatNo) {
//        String mapKey = "SEAT_MAP:" + showId;
//        Object index = redisTemplate.opsForHash().get(mapKey, seatNo);
//        return index != null ? Integer.parseInt(index.toString()) : -1;
//    }
//}



@Repository
@RequiredArgsConstructor
public class SeatLockRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_META_PREFIX = "LOCK_META:";

    public void saveLockMetadata(SeatLock seatLock, long ttlSeconds) {
        String key = buildKey(seatLock.getShowId(), seatLock.getSeatIndex());

        Map<String, String> metadata = Map.of(
            "showId", seatLock.getShowId(),
            "seatIndex", seatLock.getSeatIndex().toString(),
            "userId", String.valueOf(seatLock.getUserId()),
            "sessionId", seatLock.getSessionId(),
            "lockedAt", seatLock.getLockedAt().toString(),
            "expiresAt", seatLock.getExpiresAt().toString()
        );

        redisTemplate.opsForHash().putAll(key, metadata);
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
    }

    public Map<Object, Object> getLockMetadata(String showId, Integer seatIndex) {
        return redisTemplate.opsForHash().entries(buildKey(showId, seatIndex));
    }

    public void deleteLockMetadata(String showId, Integer seatIndex) {
        redisTemplate.delete(buildKey(showId, seatIndex));
    }

    public boolean hasLock(String showId, Integer seatIndex) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(showId, seatIndex)));
    }

    private String buildKey(String showId, Integer seatIndex) {
        return LOCK_META_PREFIX + showId + ":" + seatIndex;
    }
}
