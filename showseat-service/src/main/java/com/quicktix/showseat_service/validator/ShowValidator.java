package com.quicktix.showseat_service.validator;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.quicktix.showseat_service.dto.request.CreateShowRequest;
import com.quicktix.showseat_service.exception.BookingNotAllowedException;
import com.quicktix.showseat_service.exception.ShowOverlapException;
import com.quicktix.showseat_service.model.document.Show;
import com.quicktix.showseat_service.repository.ShowRepository;
import com.quicktix.showseat_service.util.DateTimeUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShowValidator {

    private final ShowRepository showRepository;

    public void validateShowTiming(CreateShowRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (!DateTimeUtil.isInFuture(request.getStartTime())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
    }

    public void validateNoOverlap(CreateShowRequest request) {
        boolean hasOverlap = showRepository.existsByScreenIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                request.getScreenId(),
                request.getEndTime(),
                request.getStartTime()
        );

        if (hasOverlap) {
            throw new ShowOverlapException(
                "Another show is already scheduled for this screen during the specified time");
        }
    }

    public void validateBookingAllowed(Show show) {
        if (!show.getBookingEnabled()) {
            throw new BookingNotAllowedException("Booking is not enabled for this show");
        }

        if (LocalDateTime.now().isAfter(show.getBookingCutoffTime())) {
            throw new BookingNotAllowedException("Booking cutoff time has passed for this show");
        }

        if (LocalDateTime.now().isAfter(show.getStartTime())) {
            throw new BookingNotAllowedException("Cannot book seats for a show that has already started");
        }
    }
}
