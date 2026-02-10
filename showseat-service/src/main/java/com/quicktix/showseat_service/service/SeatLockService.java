package com.quicktix.showseat_service.service;

import com.quicktix.showseat_service.dto.request.ConfirmSeatsRequest;
import com.quicktix.showseat_service.dto.request.LockSeatsRequest;
import com.quicktix.showseat_service.dto.request.UnlockSeatsRequest;
import com.quicktix.showseat_service.dto.response.LockSeatsResponse;

public interface SeatLockService {
    
    LockSeatsResponse lockSeats(LockSeatsRequest request);
    
    void unlockSeats(UnlockSeatsRequest request);
    
    void confirmSeats(ConfirmSeatsRequest request);
    
    void expireLocks(String showId);
}