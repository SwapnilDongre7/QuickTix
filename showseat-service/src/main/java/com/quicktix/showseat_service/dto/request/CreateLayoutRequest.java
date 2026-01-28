package com.quicktix.showseat_service.dto.request;

import java.util.List;

import com.quicktix.showseat_service.model.document.Row;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLayoutRequest {
    
    @NotNull(message = "Screen ID is required")
    @Positive(message = "Screen ID must be positive")
    private Long screenId;
    
    @NotBlank(message = "Layout name is required")
    @Size(min = 3, max = 100, message = "Layout name must be between 3 and 100 characters")
    private String layoutName;
    
    @NotNull(message = "Rows are required")
    @Size(min = 1, message = "At least one row is required")
    private List<Row> rows;
    
    @NotNull(message = "Total rows is required")
    @Positive(message = "Total rows must be positive")
    private Integer totalRows;
    
    @NotNull(message = "Total columns is required")
    @Positive(message = "Total columns must be positive")
    private Integer totalColumns;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
