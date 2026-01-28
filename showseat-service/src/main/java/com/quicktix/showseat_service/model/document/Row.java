package com.quicktix.showseat_service.model.document;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Row {
    
    /**
     * Row index (0-based)
     */
    private Integer rowIndex;
    
    /**
     * Row label for display (e.g., "A", "B", "C")
     */
    private String rowLabel;
    
    /**
     * List of cells (seats and spaces) in this row
     */
    private List<Cell> cells;
}
