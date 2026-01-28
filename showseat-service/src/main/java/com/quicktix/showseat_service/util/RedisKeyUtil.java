package com.quicktix.showseat_service.util;

public class RedisKeyUtil {

    private static final String SEAT_AVAILABILITY_PREFIX = "SEAT_AVAIL:";
    private static final String SEAT_LOCKS_PREFIX = "SEAT_LOCK:";
    private static final String SEAT_MAPPING_PREFIX = "SEAT_MAP:";
    private static final String LOCK_METADATA_PREFIX = "LOCK_META:";

    public static String getSeatAvailabilityKey(String showId) {
        return SEAT_AVAILABILITY_PREFIX + showId;
    }

    public static String getSeatLocksKey(String showId) {
        return SEAT_LOCKS_PREFIX + showId;
    }

    public static String getSeatMappingKey(String showId) {
        return SEAT_MAPPING_PREFIX + showId;
    }

    public static String getLockMetadataKey(String showId, Integer seatIndex) {
        return LOCK_METADATA_PREFIX + showId + ":" + seatIndex;
    }
}