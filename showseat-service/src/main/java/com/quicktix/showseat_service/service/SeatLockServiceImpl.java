//package com.quicktix.showseat_service.service;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.stereotype.Service;
//
//import com.quicktix.showseat_service.config.AppConfig;
//import com.quicktix.showseat_service.dto.request.ConfirmSeatsRequest;
//import com.quicktix.showseat_service.dto.request.LockSeatsRequest;
//import com.quicktix.showseat_service.dto.request.UnlockSeatsRequest;
//import com.quicktix.showseat_service.dto.response.LockSeatsResponse;
//import com.quicktix.showseat_service.exception.LayoutNotFoundException;
//import com.quicktix.showseat_service.exception.ShowNotFoundException;
//import com.quicktix.showseat_service.model.document.SeatLayout;
//import com.quicktix.showseat_service.model.document.Show;
//import com.quicktix.showseat_service.repository.SeatLayoutRepository;
//import com.quicktix.showseat_service.repository.ShowRepository;
//import com.quicktix.showseat_service.util.RedisBitmapUtil;
//import com.quicktix.showseat_service.validator.SeatValidator;
//import com.quicktix.showseat_service.validator.ShowValidator;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SeatLockServiceImpl implements SeatLockService {
//
//    private final ShowRepository showRepository;
//    private final SeatLayoutRepository layoutRepository;
//    private final RedisBitmapUtil redisBitmapUtil;
//    private final SeatValidator seatValidator;
//    private final ShowValidator showValidator;
//    private final ShowService showService;
//    private final AppConfig appConfig;
//
//    @Override
//    public LockSeatsResponse lockSeats(LockSeatsRequest request) {
//        Show show = showRepository.findById(request.getShowId())
//                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + request.getShowId()));
//        
//        showValidator.validateBookingAllowed(show);
//        
//        SeatLayout layout = layoutRepository.findById(show.getLayoutId())
//                .orElseThrow(() -> new LayoutNotFoundException("Layout not found: " + show.getLayoutId()));
//        
//        seatValidator.validateNoDuplicateSeats(request.getSeatNumbers());
//        seatValidator.validateSeatCount(request.getSeatNumbers(), appConfig.getSeatLock().getMaxSeatsPerBooking());
//        seatValidator.validateSeatNumbers(layout, request.getSeatNumbers());
//        
//        List<Integer> seatIndices = convertToIndices(request.getShowId(), request.getSeatNumbers());
//        
//        List<Integer> lockedIndices = redisBitmapUtil.lockSeats(
//                request.getShowId(), 
//                seatIndices, 
//                request.getUserId(), 
//                request.getSessionId()
//        );
//        
//        List<String> lockedSeats = convertToSeatNumbers(request.getShowId(), lockedIndices);
//        List<String> failedSeats = new ArrayList<>(request.getSeatNumbers());
//        failedSeats.removeAll(lockedSeats);
//        
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime expiresAt = now.plusSeconds(appConfig.getSeatLock().getTtlSeconds());
//        
//        log.info("Locked {} seats for show {} by user {}", lockedSeats.size(), request.getShowId(), request.getUserId());
//        
//        return LockSeatsResponse.builder()
//                .showId(request.getShowId())
//                .lockedSeats(lockedSeats)
//                .failedSeats(failedSeats)
//                .lockedAt(now)
//                .expiresAt(expiresAt)
//                .lockDurationSeconds(appConfig.getSeatLock().getTtlSeconds())
//                .sessionId(request.getSessionId())
//                .build();
//    }
//
//    @Override
//    public void unlockSeats(UnlockSeatsRequest request) {
//        Show show = showRepository.findById(request.getShowId())
//                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + request.getShowId()));
//        
//        List<Integer> seatIndices = convertToIndices(request.getShowId(), request.getSeatNumbers());
//        
//        redisBitmapUtil.unlockSeats(request.getShowId(), seatIndices);
//        
//        log.info("Unlocked {} seats for show {} by user {}", 
//                request.getSeatNumbers().size(), request.getShowId(), request.getUserId());
//    }
//
//    @Override
//    public void confirmSeats(ConfirmSeatsRequest request) {
//        Show show = showRepository.findById(request.getShowId())
//                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + request.getShowId()));
//        
//        List<Integer> seatIndices = convertToIndices(request.getShowId(), request.getSeatNumbers());
//        
//        redisBitmapUtil.confirmBooking(request.getShowId(), seatIndices);
//        
//        showService.updateAvailableSeats(request.getShowId(), request.getSeatNumbers().size());
//        
//        log.info("Confirmed booking for {} seats in show {} for booking {}", 
//                request.getSeatNumbers().size(), request.getShowId(), request.getBookingId());
//    }
//
//    @Override
//    public void expireLocks(String showId) {
//        redisBitmapUtil.clearShowData(showId);
//        log.info("Expired all locks for show {}", showId);
//    }
//
//    private List<Integer> convertToIndices(String showId, List<String> seatNumbers) {
//        List<Integer> indices = new ArrayList<>();
//        
//        for (String seatNo : seatNumbers) {
//            Integer index = redisBitmapUtil.getSeatIndex(showId, seatNo);
//            if (index != null) {
//                indices.add(index);
//            }
//        }
//        
//        return indices;
//    }
//
//    private List<String> convertToSeatNumbers(String showId, List<Integer> indices) {
//        List<String> seatNumbers = new ArrayList<>();
//        
//        for (Integer index : indices) {
//            String seatNo = redisBitmapUtil.getSeatNumber(showId, index);
//            if (seatNo != null) {
//                seatNumbers.add(seatNo);
//            }
//        }
//        
//        return seatNumbers;
//    }
//}


package com.quicktix.showseat_service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.quicktix.showseat_service.config.AppConfig;
import com.quicktix.showseat_service.dto.request.ConfirmSeatsRequest;
import com.quicktix.showseat_service.dto.request.LockSeatsRequest;
import com.quicktix.showseat_service.dto.request.UnlockSeatsRequest;
import com.quicktix.showseat_service.dto.response.LockSeatsResponse;
import com.quicktix.showseat_service.exception.LayoutNotFoundException;
import com.quicktix.showseat_service.exception.SeatAlreadyBookedException;
import com.quicktix.showseat_service.exception.SeatNotLockedException;
import com.quicktix.showseat_service.exception.ShowNotFoundException;
import com.quicktix.showseat_service.model.document.SeatLayout;
import com.quicktix.showseat_service.model.document.Show;
import com.quicktix.showseat_service.repository.SeatLayoutRepository;
import com.quicktix.showseat_service.repository.ShowRepository;
import com.quicktix.showseat_service.util.RedisBitmapUtil;
import com.quicktix.showseat_service.validator.SeatValidator;
import com.quicktix.showseat_service.validator.ShowValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.stereotype.Service;
//
//import com.quicktix.showseat_service.config.AppConfig;
//import com.quicktix.showseat_service.dto.request.ConfirmSeatsRequest;
//import com.quicktix.showseat_service.dto.request.LockSeatsRequest;
//import com.quicktix.showseat_service.dto.request.UnlockSeatsRequest;
//import com.quicktix.showseat_service.dto.response.LockSeatsResponse;
//import com.quicktix.showseat_service.exception.LayoutNotFoundException;
//import com.quicktix.showseat_service.exception.ShowNotFoundException;
//import com.quicktix.showseat_service.model.document.SeatLayout;
//import com.quicktix.showseat_service.model.document.Show;
//import com.quicktix.showseat_service.repository.SeatLayoutRepository;
//import com.quicktix.showseat_service.repository.ShowRepository;
//import com.quicktix.showseat_service.util.RedisBitmapUtil;
//import com.quicktix.showseat_service.validator.SeatValidator;
//import com.quicktix.showseat_service.validator.ShowValidator;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SeatLockServiceImpl implements SeatLockService {
//
//    private final ShowRepository showRepository;
//    private final SeatLayoutRepository layoutRepository;
//    private final RedisBitmapUtil redisBitmapUtil;
//    private final SeatValidator seatValidator;
//    private final ShowValidator showValidator;
//    private final ShowService showService;
//    private final AppConfig appConfig;
//
//    @Override
//    public LockSeatsResponse lockSeats(LockSeatsRequest request) {
//
//        Show show = showRepository.findById(request.getShowId())
//                .orElseThrow(() -> new ShowNotFoundException("Show not found"));
//
//        showValidator.validateBookingAllowed(show);
//
//        SeatLayout layout = layoutRepository.findById(show.getLayoutId())
//                .orElseThrow(() -> new LayoutNotFoundException("Layout not found"));
//
//        seatValidator.validateNoDuplicateSeats(request.getSeatNumbers());
//        seatValidator.validateSeatCount(
//                request.getSeatNumbers(),
//                appConfig.getSeatLock().getMaxSeatsPerBooking()
//        );
//        seatValidator.validateSeatNumbers(layout, request.getSeatNumbers());
//
//        List<Integer> seatIndices = convertToIndices(request.getShowId(), request.getSeatNumbers());
//
//        List<Integer> lockedIndices = redisBitmapUtil.lockSeats(
//                request.getShowId(),
//                seatIndices,
//                request.getUserId(),
//                request.getSessionId(),
//                appConfig.getSeatLock().getTtlSeconds()
//        );
//
//        List<String> lockedSeats = convertToSeatNumbers(request.getShowId(), lockedIndices);
//        List<String> failedSeats = new ArrayList<>(request.getSeatNumbers());
//        failedSeats.removeAll(lockedSeats);
//
//        LocalDateTime now = LocalDateTime.now();
//
//        return LockSeatsResponse.builder()
//                .showId(request.getShowId())
//                .lockedSeats(lockedSeats)
//                .failedSeats(failedSeats)
//                .lockedAt(now)
//                .expiresAt(now.plusSeconds(appConfig.getSeatLock().getTtlSeconds()))
//                .lockDurationSeconds(appConfig.getSeatLock().getTtlSeconds())
//                .sessionId(request.getSessionId())
//                .build();
//    }
//
//    @Override
//    public void unlockSeats(UnlockSeatsRequest request) {
//
//        showRepository.findById(request.getShowId())
//                .orElseThrow(() -> new ShowNotFoundException("Show not found"));
//
//        List<Integer> indices = convertToIndices(request.getShowId(), request.getSeatNumbers());
//
//        redisBitmapUtil.unlockSeats(
//                request.getShowId(),
//                indices,
//                request.getUserId(),
//                request.getSessionId()
//        );
//    }
//
//    @Override
//    public void confirmSeats(ConfirmSeatsRequest request) {
//
//        showRepository.findById(request.getShowId())
//                .orElseThrow(() -> new ShowNotFoundException("Show not found"));
//
//        List<Integer> indices = convertToIndices(request.getShowId(), request.getSeatNumbers());
//
//        redisBitmapUtil.confirmBooking(
//                request.getShowId(),
//                indices,
//                request.getUserId(),
//                request.getSessionId()
//        );
//
//        showService.updateAvailableSeats(
//                request.getShowId(),
//                request.getSeatNumbers().size()
//        );
//    }
//
//    @Override
//    public void expireLocks(String showId) {
//        redisBitmapUtil.clearLocksOnly(showId);
//    }
//
//    /* ============================================================
//       HELPERS
//       ============================================================ */
//
//    private List<Integer> convertToIndices(String showId, List<String> seatNumbers) {
//        List<Integer> indices = new ArrayList<>();
//        for (String seat : seatNumbers) {
//            Integer index = redisBitmapUtil.getSeatIndex(showId, seat);
//            if (index != null) {
//                indices.add(index);
//            }
//        }
//        return indices;
//    }
//
//    private List<String> convertToSeatNumbers(String showId, List<Integer> indices) {
//        List<String> seats = new ArrayList<>();
//        for (Integer index : indices) {
//            String seat = redisBitmapUtil.getSeatNumber(showId, index);
//            if (seat != null) {
//                seats.add(seat);
//            }
//        }
//        return seats;
//    }
//}


@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockServiceImpl implements SeatLockService {

    private final ShowRepository showRepository;
    private final SeatLayoutRepository layoutRepository;
    private final RedisBitmapUtil redisBitmapUtil;
    private final SeatValidator seatValidator;
    private final ShowValidator showValidator;
    private final ShowService showService;
    private final AppConfig appConfig;

    @Override
    public LockSeatsResponse lockSeats(LockSeatsRequest request) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + request.getShowId()));

        showValidator.validateBookingAllowed(show);

        SeatLayout layout = layoutRepository.findById(show.getLayoutId())
                .orElseThrow(() -> new LayoutNotFoundException("Layout not found: " + show.getLayoutId()));

        seatValidator.validateNoDuplicateSeats(request.getSeatNumbers());
        seatValidator.validateSeatCount(
                request.getSeatNumbers(),
                appConfig.getSeatLock().getMaxSeatsPerBooking()
        );
        seatValidator.validateSeatNumbers(layout, request.getSeatNumbers());

        List<Integer> seatIndices = convertToIndices(request.getShowId(), request.getSeatNumbers());

        if (seatIndices.isEmpty()) {
            throw new IllegalArgumentException("No valid seat indices found for the provided seat numbers");
        }

        for (Integer index : seatIndices) {
            if (redisBitmapUtil.isSeatBooked(request.getShowId(), index)) {
                String seatNo = redisBitmapUtil.getSeatNumber(request.getShowId(), index);
                throw new SeatAlreadyBookedException("Seat " + seatNo + " is already booked");
            }
        }

        List<Integer> lockedIndices = redisBitmapUtil.lockSeats(
                request.getShowId(),
                seatIndices,
                request.getUserId(),
                request.getSessionId(),
                appConfig.getSeatLock().getTtlSeconds()
        );

        List<String> lockedSeats = convertToSeatNumbers(request.getShowId(), lockedIndices);
        List<String> failedSeats = new ArrayList<>(request.getSeatNumbers());
        failedSeats.removeAll(lockedSeats);

        LocalDateTime now = LocalDateTime.now();

        log.info("Lock seats result - Success: {}, Failed: {}", lockedSeats.size(), failedSeats.size());

        return LockSeatsResponse.builder()
                .showId(request.getShowId())
                .lockedSeats(lockedSeats)
                .failedSeats(failedSeats)
                .lockedAt(now)
                .expiresAt(now.plusSeconds(appConfig.getSeatLock().getTtlSeconds()))
                .lockDurationSeconds(appConfig.getSeatLock().getTtlSeconds())
                .sessionId(request.getSessionId())
                .build();
    }

    @Override
    public void unlockSeats(UnlockSeatsRequest request) {
        showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + request.getShowId()));

        List<Integer> indices = convertToIndices(request.getShowId(), request.getSeatNumbers());

        if (indices.isEmpty()) {
            throw new IllegalArgumentException("No valid seat indices found for the provided seat numbers");
        }

        int unlockedCount = redisBitmapUtil.unlockSeats(
                request.getShowId(),
                indices,
                request.getUserId(),
                request.getSessionId()
        );

        if (unlockedCount == 0) {
            throw new SeatNotLockedException(
                    "No seats were unlocked. Seats are not locked by this user/session."
            );
        }

        log.info("Unlocked {} seats for show {}", unlockedCount, request.getShowId());
    }


    @Override
    public void confirmSeats(ConfirmSeatsRequest request) {
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + request.getShowId()));

        List<Integer> indices = convertToIndices(request.getShowId(), request.getSeatNumbers());

        if (indices.isEmpty()) {
            throw new IllegalArgumentException("No valid seat indices found for the provided seat numbers");
        }

        Map<Integer, Boolean> validationResults = redisBitmapUtil.validateSeatsLockedByUser(
                request.getShowId(),
                indices,
                request.getUserId(),
                request.getSessionId()
        );

        List<String> notLockedSeats = validationResults.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(entry -> redisBitmapUtil.getSeatNumber(request.getShowId(), entry.getKey()))
                .collect(Collectors.toList());

        if (!notLockedSeats.isEmpty()) {
            throw new SeatNotLockedException(
                    "Cannot confirm booking. Seats not locked by this user/session: " + notLockedSeats
            );
        }

        for (Integer index : indices) {
            if (redisBitmapUtil.isSeatBooked(request.getShowId(), index)) {
                String seatNo = redisBitmapUtil.getSeatNumber(request.getShowId(), index);
                throw new SeatAlreadyBookedException("Seat " + seatNo + " is already booked");
            }
        }

        redisBitmapUtil.confirmBooking(
                request.getShowId(),
                indices,
                request.getUserId(),
                request.getSessionId()
        );

        showService.updateAvailableSeats(
                request.getShowId(),
                request.getSeatNumbers().size()
        );

        log.info("Confirm seats completed for show {}, booking {}", request.getShowId(), request.getBookingId());
    }

    @Override
    public void expireLocks(String showId) {
        redisBitmapUtil.clearLocksOnly(showId);
        log.info("Expired all locks for show {}", showId);
    }

    private List<Integer> convertToIndices(String showId, List<String> seatNumbers) {
        List<Integer> indices = new ArrayList<>();
        for (String seat : seatNumbers) {
            Integer index = redisBitmapUtil.getSeatIndex(showId, seat);
            if (index != null) {
                indices.add(index);
            } else {
                log.warn("Seat {} not found in mapping for show {}", seat, showId);
            }
        }
        return indices;
    }

    private List<String> convertToSeatNumbers(String showId, List<Integer> indices) {
        List<String> seats = new ArrayList<>();
        for (Integer index : indices) {
            String seat = redisBitmapUtil.getSeatNumber(showId, index);
            if (seat != null) {
                seats.add(seat);
            } else {
                log.warn("Index {} not found in mapping for show {}", index, showId);
            }
        }
        return seats;
    }
}