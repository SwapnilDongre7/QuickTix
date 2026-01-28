//package com.quicktix.showseat_service.dto.request;
//
//import java.util.List;
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
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class UnlockSeatsRequest {
//    
//    @NotBlank(message = "Show ID is required")
//    private String showId;
//    
//    @NotNull(message = "User ID is required")
//    @Positive(message = "User ID must be positive")
//    private Long userId;
//    
//    @NotNull(message = "Seat numbers are required")
//    @Size(min = 1, message = "At least one seat number is required")
//    private List<@NotBlank(message = "Seat number cannot be blank") String> seatNumbers;
//}


package com.quicktix.showseat_service.dto.request;

import java.util.List;

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
public class UnlockSeatsRequest {

    @NotBlank(message = "Show ID is required")
    private String showId;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotBlank(message = "Session ID is required")
    private String sessionId;   // ✅ ADDED

    @NotNull(message = "Seat numbers are required")
    @Size(min = 1, message = "At least one seat number is required")
    private List<@NotBlank(message = "Seat number cannot be blank") String> seatNumbers;
}
