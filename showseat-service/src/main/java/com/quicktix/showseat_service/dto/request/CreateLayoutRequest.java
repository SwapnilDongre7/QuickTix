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

/**
 * Request DTO for creating a seat layout.
 * 
 * screenId is OPTIONAL - layouts can be created as reusable templates
 * without being bound to a specific screen. When null, the layout
 * belongs to the owner (via createdBy) and can be used across any of
 * their screens.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLayoutRequest {

    /**
     * Screen ID (OPTIONAL)
     * If null, creates a reusable layout template for the owner.
     * If provided, creates a layout bound to that specific screen.
     */
    @Positive(message = "Screen ID must be positive if provided")
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



//package com.quicktix.showseat_service.dto.request;
//
//import java.util.List;
//
//import com.quicktix.showseat_service.model.document.Row;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Positive;
//import jakarta.validation.constraints.Size;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
///**
// * Request DTO for creating a seat layout.
// * 
// * screenId is OPTIONAL - layouts can be created as reusable templates
// * without being bound to a specific screen. When null, the layout
// * belongs to the owner (via createdBy) and can be used across any of
// * their screens.
// */
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class CreateLayoutRequest {
//
//    /**
//     * Screen ID (OPTIONAL)
//     * If null, creates a reusable layout template for the owner.
//     * If provided, creates a layout bound to that specific screen.
//     */
//    @Positive(message = "Screen ID must be positive if provided")
//    private Long screenId;
//
//    @NotBlank(message = "Layout name is required")
//    @Size(min = 3, max = 100, message = "Layout name must be between 3 and 100 characters")
//    private String layoutName;
//
//    @NotNull(message = "Rows are required")
//    @Size(min = 1, message = "At least one row is required")
//    private List<Row> rows;
//
//    @NotNull(message = "Total rows is required")
//    @Positive(message = "Total rows must be positive")
//    private Integer totalRows;
//
//    @NotNull(message = "Total columns is required")
//    @Positive(message = "Total columns must be positive")
//    private Integer totalColumns;
//
//    @Size(max = 500, message = "Description cannot exceed 500 characters")
//    private String description;
//}
