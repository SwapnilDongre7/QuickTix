package com.quicktix.showseat_service.service;

import java.util.List;

import com.quicktix.showseat_service.dto.request.CreateLayoutRequest;
import com.quicktix.showseat_service.dto.response.SeatLayoutResponse;

public interface SeatLayoutService {
    
    SeatLayoutResponse createLayout(CreateLayoutRequest request, Long createdBy);
    
    SeatLayoutResponse getLayoutById(String layoutId);
    
    List<SeatLayoutResponse> getLayoutsByScreen(Long screenId);
    
    SeatLayoutResponse getActiveLayoutByScreen(Long screenId);
    
    SeatLayoutResponse deactivateLayout(String layoutId);
    
    void deleteLayout(String layoutId);
    
    
}