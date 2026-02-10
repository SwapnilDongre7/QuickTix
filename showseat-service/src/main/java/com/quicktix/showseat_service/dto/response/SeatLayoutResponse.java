package com.quicktix.showseat_service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.quicktix.showseat_service.model.document.Row;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLayoutResponse {
    
    private String id;
    private Long screenId;
    private String layoutName;
    private List<Row> rows;
    private Integer totalRows;
    private Integer totalColumns;
    private Integer totalSeats;
    private Boolean isActive;
    private Integer version;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
