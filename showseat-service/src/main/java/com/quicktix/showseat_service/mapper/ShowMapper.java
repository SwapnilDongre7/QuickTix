package com.quicktix.showseat_service.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.quicktix.showseat_service.config.AppConfig;
import com.quicktix.showseat_service.dto.request.CreateShowRequest;
import com.quicktix.showseat_service.dto.response.ShowResponse;
import com.quicktix.showseat_service.enums.ShowStatus;
import com.quicktix.showseat_service.model.document.Show;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShowMapper {

    private final AppConfig appConfig;

    public Show toEntity(CreateShowRequest request, Integer totalSeats, Long createdBy) {
        LocalDateTime bookingCutoffTime = request.getStartTime()
                .minusMinutes(appConfig.getShow().getBookingCutoffMinutes());

        return Show.builder()
                .movieId(request.getMovieId())
                .theatreId(request.getTheatreId())
                .screenId(request.getScreenId())
                .layoutId(request.getLayoutId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .pricing(request.getPricing())
                .status(ShowStatus.SCHEDULED)
                .language(request.getLanguage())
                .format(request.getFormat())
                .availableSeats(totalSeats)
                .bookedSeats(0)
                .bookingEnabled(true)
                .bookingCutoffTime(bookingCutoffTime)
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();
    }

    public ShowResponse toResponse(Show show) {
        return ShowResponse.builder()
                .id(show.getId())
                .movieId(show.getMovieId())
                .theatreId(show.getTheatreId())
                .screenId(show.getScreenId())
                .layoutId(show.getLayoutId())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .pricing(show.getPricing())
                .status(show.getStatus())
                .language(show.getLanguage())
                .format(show.getFormat())
                .availableSeats(show.getAvailableSeats())
                .bookedSeats(show.getBookedSeats())
                .bookingEnabled(show.getBookingEnabled())
                .bookingCutoffTime(show.getBookingCutoffTime())
                .notes(show.getNotes())
                .createdAt(show.getCreatedAt())
                .build();
    }
}