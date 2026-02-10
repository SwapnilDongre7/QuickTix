package com.quicktix.showseat_service.service;

import java.time.LocalDateTime;
import java.util.List;

import com.quicktix.showseat_service.dto.request.CreateShowRequest;
import com.quicktix.showseat_service.dto.response.ShowResponse;
import com.quicktix.showseat_service.enums.ShowStatus;

public interface ShowService {
    
    ShowResponse createShow(CreateShowRequest request, Long createdBy);
    
    ShowResponse getShowById(String showId);
    
    List<ShowResponse> getShowsByMovie(Long movieId);
    
    List<ShowResponse> getShowsByTheatre(Long theatreId);
    
    List<ShowResponse> getShowsByDateRange(LocalDateTime startDate, LocalDateTime endDate, ShowStatus status);
    
    ShowResponse updateShowStatus(String showId, ShowStatus status);
    
    void deleteShow(String showId);
    
    void updateAvailableSeats(String showId, int bookedCount);

}
