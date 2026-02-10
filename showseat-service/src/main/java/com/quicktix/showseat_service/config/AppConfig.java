package com.quicktix.showseat_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private SeatLock seatLock = new SeatLock();
    private Show show = new Show();

    @Data
    public static class SeatLock {
        private int ttlSeconds = 300; // 5 minutes lock duration
        private int maxSeatsPerBooking = 10;
    }

    @Data
    public static class Show {
        private int bookingCutoffMinutes = 30;
    }
}