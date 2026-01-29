package com.quicktix.showseat_service.model.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.quicktix.showseat_service.enums.ShowStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shows")
@CompoundIndexes({
    @CompoundIndex(name = "movie_theatre_time_idx", 
                   def = "{'movieId': 1, 'theatreId': 1, 'startTime': 1}"),
    @CompoundIndex(name = "screen_time_idx", 
                   def = "{'screenId': 1, 'startTime': 1}")
})
public class Show {
    
    /**
     * Unique show identifier
     */
    @Id
    private String id;
    
    /**
     * Reference to movie ID from Catalogue Service
     */
    private Long movieId;
    
    /**
     * Reference to theatre ID from Theatre Management Service
     */
    private Long theatreId;
    
    /**
     * Reference to screen ID from Theatre Management Service
     */
    private Long screenId;
    
    /**
     * Reference to seat layout ID
     */
    private String layoutId;
    
    /**
     * Show start time
     */
    private LocalDateTime startTime;
    
    /**
     * Show end time (calculated based on movie duration)
     */
    private LocalDateTime endTime;
    
    /**
     * Pricing for different seat types
     */
    private Pricing pricing;
    
    /**
     * Current status of the show
     */
    private ShowStatus status;
    
    /**
     * Language of the show (if different from movie default)
     */
    private String language;
    
    /**
     * Show format (2D, 3D, IMAX, 4DX, etc.)
     */
    private String format;
    
    /**
     * Available seats count (denormalized for quick access)
     */
    private Integer availableSeats;
    
    /**
     * Booked seats count (denormalized for quick access)
     */
    private Integer bookedSeats;
    
    /**
     * Whether online booking is enabled
     */
    private Boolean bookingEnabled;
    
    /**
     * Show booking cutoff time (e.g., 30 mins before start)
     */
    private LocalDateTime bookingCutoffTime;
    
    /**
     * Additional notes/information
     */
    private String notes;
    
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
     * Theatre owner ID who created this show
     */
    private Long createdBy;
}
