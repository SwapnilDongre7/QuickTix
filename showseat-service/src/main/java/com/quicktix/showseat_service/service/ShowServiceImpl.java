package com.quicktix.showseat_service.service;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quicktix.showseat_service.dto.request.CreateShowRequest;
import com.quicktix.showseat_service.dto.response.ShowResponse;
import com.quicktix.showseat_service.enums.ShowStatus;
import com.quicktix.showseat_service.exception.LayoutNotFoundException;
import com.quicktix.showseat_service.exception.ShowNotFoundException;
import com.quicktix.showseat_service.mapper.ShowMapper;
import com.quicktix.showseat_service.model.document.SeatLayout;
import com.quicktix.showseat_service.model.document.Show;
import com.quicktix.showseat_service.repository.SeatLayoutRepository;
import com.quicktix.showseat_service.repository.ShowRepository;
import com.quicktix.showseat_service.util.RedisBitmapUtil;
import com.quicktix.showseat_service.validator.ShowValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final SeatLayoutRepository layoutRepository;
    private final ShowMapper showMapper;
    private final ShowValidator showValidator;
    private final RedisBitmapUtil redisBitmapUtil;

    @Override
    @Transactional
    public ShowResponse createShow(CreateShowRequest request, Long createdBy) {
        showValidator.validateShowTiming(request);
        showValidator.validateNoOverlap(request);
        
        SeatLayout layout = layoutRepository.findById(request.getLayoutId())
                .orElseThrow(() -> new LayoutNotFoundException("Layout not found: " + request.getLayoutId()));
        
        Show show = showMapper.toEntity(request, layout.getTotalSeats(), createdBy);
        Show savedShow = showRepository.save(show);
        
        initializeShowSeats(savedShow, layout);
        
        log.info("Created show {} for movie {} at theatre {}", 
                savedShow.getId(), savedShow.getMovieId(), savedShow.getTheatreId());
        
        return showMapper.toResponse(savedShow);
    }

    @Override
    public ShowResponse getShowById(String showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + showId));
        
        return showMapper.toResponse(show);
    }

    @Override
    public List<ShowResponse> getShowsByMovie(Long movieId) {
        List<Show> shows = showRepository.findByMovieIdAndStatusOrderByStartTimeAsc(
                movieId, ShowStatus.SCHEDULED);
        
        return shows.stream()
                .map(showMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowResponse> getShowsByTheatre(Long theatreId) {
        List<Show> shows = showRepository.findByTheatreIdAndStatusOrderByStartTimeAsc(
                theatreId, ShowStatus.SCHEDULED);
        
        return shows.stream()
                .map(showMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowResponse> getShowsByDateRange(LocalDateTime startDate, LocalDateTime endDate, ShowStatus status) {
        List<Show> shows = showRepository.findShowsByDateRangeAndStatus(startDate, endDate, status);
        
        return shows.stream()
                .map(showMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShowResponse updateShowStatus(String showId, ShowStatus status) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + showId));
        
        show.setStatus(status);
        Show updated = showRepository.save(show);
        
        log.info("Updated show {} status to {}", showId, status);
        return showMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteShow(String showId) {
        if (!showRepository.existsById(showId)) {
            throw new ShowNotFoundException("Show not found: " + showId);
        }
        
        redisBitmapUtil.clearShowData(showId);
        showRepository.deleteById(showId);
        
        log.info("Deleted show {}", showId);
    }

    @Override
    @Transactional
    public void updateAvailableSeats(String showId, int bookedCount) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found: " + showId));
        
        show.setBookedSeats(show.getBookedSeats() + bookedCount);
        show.setAvailableSeats(show.getAvailableSeats() - bookedCount);
        
        showRepository.save(show);
        log.debug("Updated seat counts for show {}: booked={}, available={}", 
                showId, show.getBookedSeats(), show.getAvailableSeats());
    }

    private void initializeShowSeats(Show show, SeatLayout layout) {
        redisBitmapUtil.initializeSeatAvailability(show.getId(), layout.getTotalSeats());
        
        Map<String, Integer> seatMapping = buildSeatMapping(layout);
        redisBitmapUtil.storeSeatMapping(show.getId(), seatMapping);
        
        log.debug("Initialized {} seats for show {}", layout.getTotalSeats(), show.getId());
    }

    private Map<String, Integer> buildSeatMapping(SeatLayout layout) {
        Map<String, Integer> mapping = new HashMap<>();
        int index = 0;
        
        for (var row : layout.getRows()) {
            for (var cell : row.getCells()) {
                if (cell.getSeatNo() != null && !cell.getSeatNo().isEmpty()) {
                    mapping.put(cell.getSeatNo(), index++);
                }
            }
        }
        
        return mapping;
    }
}