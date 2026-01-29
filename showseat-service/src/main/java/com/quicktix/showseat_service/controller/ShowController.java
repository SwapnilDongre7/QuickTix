package com.quicktix.showseat_service.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quicktix.showseat_service.dto.request.CreateShowRequest;
import com.quicktix.showseat_service.dto.response.ShowResponse;
import com.quicktix.showseat_service.enums.ShowStatus;
import com.quicktix.showseat_service.service.ShowService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    /* ============================================================
       CREATE SHOW
       ============================================================ */
    @PostMapping
    public ResponseEntity<ShowResponse> createShow(
            @RequestBody CreateShowRequest request,
            @RequestParam Long createdBy
    ) {
        ShowResponse response = showService.createShow(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /* ============================================================
       GET SHOWS
       ============================================================ */
    @GetMapping("/{showId}")
    public ResponseEntity<ShowResponse> getShowById(@PathVariable String showId) {
        ShowResponse response = showService.getShowById(showId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowResponse>> getShowsByMovie(@PathVariable Long movieId) {
        List<ShowResponse> shows = showService.getShowsByMovie(movieId);
        return ResponseEntity.ok(shows);
    }

    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<List<ShowResponse>> getShowsByTheatre(@PathVariable Long theatreId) {
        List<ShowResponse> shows = showService.getShowsByTheatre(theatreId);
        return ResponseEntity.ok(shows);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ShowResponse>> getShowsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam ShowStatus status
    ) {
        List<ShowResponse> shows = showService.getShowsByDateRange(startDate, endDate, status);
        return ResponseEntity.ok(shows);
    }

    /* ============================================================
       UPDATE SHOW STATUS
       ============================================================ */
    @PatchMapping("/{showId}/status")
    public ResponseEntity<ShowResponse> updateShowStatus(
            @PathVariable String showId,
            @RequestParam ShowStatus status
    ) {
        ShowResponse response = showService.updateShowStatus(showId, status);
        return ResponseEntity.ok(response);
    }

    /* ============================================================
       DELETE SHOW
       ============================================================ */
    @DeleteMapping("/{showId}")
    public ResponseEntity<Void> deleteShow(@PathVariable String showId) {
        showService.deleteShow(showId);
        return ResponseEntity.noContent().build();
    }
}