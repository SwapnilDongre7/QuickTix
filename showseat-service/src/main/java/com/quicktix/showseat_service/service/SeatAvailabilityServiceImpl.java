//package com.quicktix.showseat_service.service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.stereotype.Service;
//
//import com.quicktix.showseat_service.dto.response.SeatAvailabilityResponse;
//import com.quicktix.showseat_service.enums.SeatStatus;
//import com.quicktix.showseat_service.exception.LayoutNotFoundException;
//import com.quicktix.showseat_service.exception.ShowNotFoundException;
//import com.quicktix.showseat_service.model.document.Cell;
//import com.quicktix.showseat_service.model.document.SeatLayout;
//import com.quicktix.showseat_service.model.document.Show;
//import com.quicktix.showseat_service.repository.SeatLayoutRepository;
//import com.quicktix.showseat_service.repository.ShowRepository;
//import com.quicktix.showseat_service.util.RedisBitmapUtil;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SeatAvailabilityServiceImpl implements SeatAvailabilityService {
//
//    private final ShowRepository showRepository;
//    private final SeatLayoutRepository layoutRepository;
//    private final RedisBitmapUtil redisBitmapUtil;
//
//    @Override
//    public SeatAvailabilityResponse getSeatAvailability(String showId) {
//        Show show = showRepository.findById(showId)
//                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + showId));
//        
//        SeatLayout layout = layoutRepository.findById(show.getLayoutId())
//                .orElseThrow(() -> new LayoutNotFoundException("Layout not found: " + show.getLayoutId()));
//        
//        Map<String, SeatStatus> seatStatusMap = buildSeatStatusMap(showId, layout);
//        
//        long availableCount = seatStatusMap.values().stream()
//                .filter(status -> status == SeatStatus.AVAILABLE)
//                .count();
//        
//        long bookedCount = seatStatusMap.values().stream()
//                .filter(status -> status == SeatStatus.BOOKED)
//                .count();
//        
//        long lockedCount = seatStatusMap.values().stream()
//                .filter(status -> status == SeatStatus.LOCKED)
//                .count();
//        
//        return SeatAvailabilityResponse.builder()
//                .showId(showId)
//                .totalSeats(layout.getTotalSeats())
//                .availableSeats((int) availableCount)
//                .bookedSeats((int) bookedCount)
//                .lockedSeats((int) lockedCount)
//                .seatStatusMap(seatStatusMap)
//                .build();
//    }
//
//    @Override
//    public boolean isSeatAvailable(String showId, String seatNo) {
//        Integer index = redisBitmapUtil.getSeatIndex(showId, seatNo);
//        
//        if (index == null) {
//            return false;
//        }
//        
//        return redisBitmapUtil.isSeatAvailable(showId, index);
//    }
//
//    private Map<String, SeatStatus> buildSeatStatusMap(String showId, SeatLayout layout) {
//        Map<String, SeatStatus> statusMap = new HashMap<>();
//        
//        for (var row : layout.getRows()) {
//            for (Cell cell : row.getCells()) {
//                if (cell.getSeatNo() != null && !cell.getSeatNo().isEmpty()) {
//                    Integer index = redisBitmapUtil.getSeatIndex(showId, cell.getSeatNo());
//                    
//                    if (index != null) {
//                        SeatStatus status = determineSeatStatus(showId, index);
//                        statusMap.put(cell.getSeatNo(), status);
//                    }
//                }
//            }
//        }
//        
//        return statusMap;
//    }
//
//    private SeatStatus determineSeatStatus(String showId, Integer index) {
//        if (redisBitmapUtil.isSeatBooked(showId, index)) {
//            return SeatStatus.BOOKED;
//        }
//        
//        if (redisBitmapUtil.isSeatLocked(showId, index)) {
//            return SeatStatus.LOCKED;
//        }
//        
//        return SeatStatus.AVAILABLE;
//    }
//}



package com.quicktix.showseat_service.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.quicktix.showseat_service.dto.response.SeatAvailabilityResponse;
import com.quicktix.showseat_service.enums.SeatStatus;
import com.quicktix.showseat_service.exception.LayoutNotFoundException;
import com.quicktix.showseat_service.exception.ShowNotFoundException;
import com.quicktix.showseat_service.model.document.Cell;
import com.quicktix.showseat_service.model.document.SeatLayout;
import com.quicktix.showseat_service.model.document.Show;
import com.quicktix.showseat_service.repository.SeatLayoutRepository;
import com.quicktix.showseat_service.repository.ShowRepository;
import com.quicktix.showseat_service.util.RedisBitmapUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatAvailabilityServiceImpl implements SeatAvailabilityService {

    private final ShowRepository showRepository;
    private final SeatLayoutRepository layoutRepository;
    private final RedisBitmapUtil redisBitmapUtil;

    @Override
    public SeatAvailabilityResponse getSeatAvailability(String showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + showId));

        SeatLayout layout = layoutRepository.findById(show.getLayoutId())
                .orElseThrow(() -> new LayoutNotFoundException("Layout not found: " + show.getLayoutId()));

        Map<String, SeatStatus> seatStatusMap = buildSeatStatusMap(showId, layout);

        long availableCount = seatStatusMap.values().stream()
                .filter(status -> status == SeatStatus.AVAILABLE)
                .count();

        long bookedCount = seatStatusMap.values().stream()
                .filter(status -> status == SeatStatus.BOOKED)
                .count();

        long lockedCount = seatStatusMap.values().stream()
                .filter(status -> status == SeatStatus.LOCKED)
                .count();

        return SeatAvailabilityResponse.builder()
                .showId(showId)
                .totalSeats(layout.getTotalSeats())
                .availableSeats((int) availableCount)
                .bookedSeats((int) bookedCount)
                .lockedSeats((int) lockedCount)
                .seatStatusMap(seatStatusMap)
                .build();
    }

    @Override
    public boolean isSeatAvailable(String showId, String seatNo) {
        Integer index = redisBitmapUtil.getSeatIndex(showId, seatNo);

        if (index == null) {
            log.warn("Seat {} not found in Redis for show {}", seatNo, showId);
            return false;
        }

        return redisBitmapUtil.isSeatAvailable(showId, index);
    }

    private Map<String, SeatStatus> buildSeatStatusMap(String showId, SeatLayout layout) {
        Map<String, SeatStatus> statusMap = new HashMap<>();

        for (var row : layout.getRows()) {
            for (Cell cell : row.getCells()) {
                if (cell.getSeatNo() != null && !cell.getSeatNo().isEmpty()) {
                    Integer index = redisBitmapUtil.getSeatIndex(showId, cell.getSeatNo());

                    if (index != null) {
                        SeatStatus status = determineSeatStatus(showId, index);
                        statusMap.put(cell.getSeatNo(), status);
                    } else {
                        log.warn("Seat {} in layout {} missing from Redis mapping", cell.getSeatNo(), layout.getId());
                        // Optional: mark as AVAILABLE or UNKNOWN
                        statusMap.put(cell.getSeatNo(), SeatStatus.AVAILABLE);
                    }
                }
            }
        }

        return statusMap;
    }

    private SeatStatus determineSeatStatus(String showId, Integer index) {
        if (redisBitmapUtil.isSeatBooked(showId, index)) return SeatStatus.BOOKED;
        if (redisBitmapUtil.isSeatLocked(showId, index)) return SeatStatus.LOCKED;
        return SeatStatus.AVAILABLE;
    }
}
