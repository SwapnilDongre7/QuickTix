package com.quicktix.showseat_service.enums;

public enum ShowStatus {
    /**
     * Show is scheduled and bookings are open
     */
    SCHEDULED,
    
    /**
     * Show is currently running
     */
    RUNNING,
    
    /**
     * Show has completed
     */
    COMPLETED,
    
    /**
     * Show has been cancelled
     */
    CANCELLED,
    
    /**
     * Show bookings are temporarily suspended
     */
    SUSPENDED
}