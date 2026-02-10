package com.quicktix.showseat_service.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quicktix.showseat_service.dto.request.CreateLayoutRequest;
import com.quicktix.showseat_service.dto.response.SeatLayoutResponse;
import com.quicktix.showseat_service.exception.LayoutNotFoundException;
import com.quicktix.showseat_service.mapper.SeatLayoutMapper;
import com.quicktix.showseat_service.model.document.Cell;
import com.quicktix.showseat_service.model.document.Row;
import com.quicktix.showseat_service.model.document.SeatLayout;
import com.quicktix.showseat_service.repository.SeatLayoutRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLayoutServiceImpl implements SeatLayoutService {

    private final SeatLayoutRepository layoutRepository;
    private final SeatLayoutMapper layoutMapper;

    @Override
    @Transactional
    public SeatLayoutResponse createLayout(CreateLayoutRequest request, Long createdBy) {
        validateLayoutRequest(request);

        SeatLayout layout = layoutMapper.toEntity(request, createdBy);

        SeatLayout savedLayout = layoutRepository.save(layout);

        if (savedLayout.getScreenId() != null) {
            log.info("Created seat layout {} for screen {}", savedLayout.getId(), savedLayout.getScreenId());
        } else {
            log.info("Created reusable seat layout {} for owner {}", savedLayout.getId(), createdBy);
        }

        return layoutMapper.toResponse(savedLayout);
    }

    @Override
    public SeatLayoutResponse getLayoutById(String layoutId) {
        SeatLayout layout = layoutRepository.findById(layoutId)
                .orElseThrow(() -> new LayoutNotFoundException("Layout not found with id: " + layoutId));

        return layoutMapper.toResponse(layout);
    }

    @Override
    public List<SeatLayoutResponse> getLayoutsByScreen(Long screenId) {
        List<SeatLayout> layouts = layoutRepository.findByScreenIdOrderByVersionDesc(screenId);

        return layouts.stream()
                .map(layoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SeatLayoutResponse getActiveLayoutByScreen(Long screenId) {
        SeatLayout layout = layoutRepository.findTopByScreenIdOrderByVersionDesc(screenId)
                .filter(SeatLayout::getIsActive)
                .orElseThrow(() -> new LayoutNotFoundException("No active layout found for screen: " + screenId));

        return layoutMapper.toResponse(layout);
    }

    @Override
    @Transactional
    public SeatLayoutResponse deactivateLayout(String layoutId) {
        SeatLayout layout = layoutRepository.findById(layoutId)
                .orElseThrow(() -> new LayoutNotFoundException("Layout not found with id: " + layoutId));

        layout.setIsActive(false);
        SeatLayout updated = layoutRepository.save(layout);

        log.info("Deactivated layout {}", layoutId);
        return layoutMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteLayout(String layoutId) {
        if (!layoutRepository.existsById(layoutId)) {
            throw new LayoutNotFoundException("Layout not found with id: " + layoutId);
        }

        layoutRepository.deleteById(layoutId);
        log.info("Deleted layout {}", layoutId);
    }

    private void validateLayoutRequest(CreateLayoutRequest request) {
        if (request.getRows() == null || request.getRows().isEmpty()) {
            throw new IllegalArgumentException("Layout must have at least one row");
        }

        Set<String> seatNumbers = new HashSet<>();

        for (Row row : request.getRows()) {
            if (row.getCells() == null || row.getCells().isEmpty()) {
                throw new IllegalArgumentException("Row must have at least one cell");
            }

            for (Cell cell : row.getCells()) {
                if (cell.getSeatNo() != null && !cell.getSeatNo().isEmpty()) {
                    if (!seatNumbers.add(cell.getSeatNo())) {
                        throw new IllegalArgumentException("Duplicate seat number: " + cell.getSeatNo());
                    }
                }
            }
        }

        if (seatNumbers.isEmpty()) {
            throw new IllegalArgumentException("Layout must have at least one seat");
        }
    }

    @Override
    public List<SeatLayoutResponse> getLayoutsByOwner(Long ownerId) {
        List<SeatLayout> layouts = layoutRepository.findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(ownerId);
        log.info("Found {} layouts for owner {}", layouts.size(), ownerId);

        return layouts.stream()
                .map(layoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatLayoutResponse> getAllLayouts() {
        List<SeatLayout> layouts = layoutRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        log.info("Found {} active layouts", layouts.size());

        return layouts.stream()
                .map(layoutMapper::toResponse)
                .collect(Collectors.toList());
    }
}