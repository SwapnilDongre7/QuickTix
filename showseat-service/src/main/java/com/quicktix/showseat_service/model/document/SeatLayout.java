package com.quicktix.showseat_service.model.document;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "seat_layouts")
public class SeatLayout {
    
    /**
     * Unique layout identifier
     */
    @Id
    private String id;
    
    /**
     * Reference to screen ID from Theatre Management Service
     */
    private Long screenId;
    
    /**
     * Layout name/version (e.g., "Standard_v1", "IMAX_Layout")
     */
    private String layoutName;
    
    /**
     * List of rows containing cells (seats/spaces)
     */
    private List<Row> rows;
    
    /**
     * Total number of rows in the layout
     */
    private Integer totalRows;
    
    /**
     * Maximum number of columns across all rows
     */
    private Integer totalColumns;
    
    /**
     * Total bookable seats (excludes SPACE cells)
     */
    private Integer totalSeats;
    
    /**
     * Whether this layout is currently active
     */
    private Boolean isActive;
    
    /**
     * Layout version for tracking changes
     */
    private Integer version;
    
    /**
     * Additional metadata (optional)
     */
    private String description;
    
    /**
     * Creation timestamp
     */
    @CreatedDate
    private LocalDateTime createdAt;
    
    /**
     * Last modification timestamp
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * User ID who created this layout (from Identity Service)
     */
    private Long createdBy;
}
