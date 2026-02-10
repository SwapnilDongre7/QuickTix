package com.quicktix.showseat_service.enums;

public enum CellType {
    /**
     * Regular seat that can be booked
     */
    SEAT,
    
    /**
     * Empty space (aisle, walkway, gap between sections)
     */
    SPACE,
    
    /**
     * Wheelchair accessible seat
     */
    WHEELCHAIR,
    
    /**
     * Premium seat (recliner, couple seat, etc.)
     */
    PREMIUM
}
