package com.quicktix.showseat_service.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.quicktix.showseat_service.exception.InvalidSeatException;
import com.quicktix.showseat_service.model.document.Cell;
import com.quicktix.showseat_service.model.document.Row;
import com.quicktix.showseat_service.model.document.SeatLayout;

@Component
public class SeatValidator {

    public void validateSeatNumbers(SeatLayout layout, List<String> seatNumbers) {
        Set<String> validSeats = getAllValidSeats(layout);
        
        for (String seatNo : seatNumbers) {
            if (!validSeats.contains(seatNo)) {
                throw new InvalidSeatException("Invalid seat number: " + seatNo);
            }
        }
    }

    public void validateNoDuplicateSeats(List<String> seatNumbers) {
        Set<String> uniqueSeats = new HashSet<>(seatNumbers);
        if (uniqueSeats.size() != seatNumbers.size()) {
            throw new InvalidSeatException("Duplicate seat numbers found in request");
        }
    }

    public void validateSeatCount(List<String> seatNumbers, int maxSeats) {
        if (seatNumbers.size() > maxSeats) {
            throw new InvalidSeatException(
                String.format("Cannot book more than %d seats at once", maxSeats));
        }
    }

    private Set<String> getAllValidSeats(SeatLayout layout) {
        Set<String> validSeats = new HashSet<>();
        
        for (Row row : layout.getRows()) {
            for (Cell cell : row.getCells()) {
                if (cell.getSeatNo() != null && !cell.getSeatNo().isEmpty()) {
                    validSeats.add(cell.getSeatNo());
                }
            }
        }
        
        return validSeats;
    }
}
