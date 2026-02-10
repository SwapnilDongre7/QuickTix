package com.quicktix.showseat_service.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.quicktix.showseat_service.dto.request.CreateLayoutRequest;
import com.quicktix.showseat_service.dto.response.SeatLayoutResponse;
import com.quicktix.showseat_service.enums.CellType;
import com.quicktix.showseat_service.model.document.Row;
import com.quicktix.showseat_service.model.document.SeatLayout;

@Component
public class SeatLayoutMapper {

    public SeatLayout toEntity(CreateLayoutRequest request, Long createdBy) {
        int totalSeats = calculateTotalSeats(request.getRows());

        return SeatLayout.builder()
                .screenId(request.getScreenId())
                .layoutName(request.getLayoutName())
                .rows(request.getRows())
                .totalRows(request.getTotalRows())
                .totalColumns(request.getTotalColumns())
                .totalSeats(totalSeats)
                .isActive(true)
                .version(1)
                .description(request.getDescription())
                .createdBy(createdBy)
                .build();
    }

    public SeatLayoutResponse toResponse(SeatLayout layout) {
        return SeatLayoutResponse.builder()
                .id(layout.getId())
                .screenId(layout.getScreenId())
                .layoutName(layout.getLayoutName())
                .rows(layout.getRows())
                .totalRows(layout.getTotalRows())
                .totalColumns(layout.getTotalColumns())
                .totalSeats(layout.getTotalSeats())
                .isActive(layout.getIsActive())
                .version(layout.getVersion())
                .description(layout.getDescription())
                .createdAt(layout.getCreatedAt())
                .updatedAt(layout.getUpdatedAt())
                .build();
    }

    private int calculateTotalSeats(List<Row> rows) {
        return rows.stream()
                .mapToInt(row ->
                        (int) row.getCells().stream()
                                .filter(cell ->
                                        cell.getType() == CellType.SEAT ||
                                        cell.getType() == CellType.WHEELCHAIR ||
                                        cell.getType() == CellType.PREMIUM
                                )
                                .count()
                )
                .sum();
    }
}