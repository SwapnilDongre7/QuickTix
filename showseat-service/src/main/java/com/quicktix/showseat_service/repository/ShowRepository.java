package com.quicktix.showseat_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.quicktix.showseat_service.enums.ShowStatus;
import com.quicktix.showseat_service.model.document.Show;

@Repository
public interface ShowRepository extends MongoRepository<Show, String> {

    List<Show> findByMovieIdAndTheatreIdAndStartTimeBetween(
            Long movieId, Long theatreId, LocalDateTime startTime, LocalDateTime endTime);

    List<Show> findByTheatreIdAndScreenIdAndStartTimeBetween(
            Long theatreId, Long screenId, LocalDateTime startTime, LocalDateTime endTime);

    List<Show> findByMovieIdAndStatusOrderByStartTimeAsc(Long movieId, ShowStatus status);

    List<Show> findByTheatreIdAndStatusOrderByStartTimeAsc(Long theatreId, ShowStatus status);

    List<Show> findByScreenIdAndStatus(Long screenId, ShowStatus status);

    @Query("{ 'startTime': { $gte: ?0, $lte: ?1 }, 'status': ?2 }")
    List<Show> findShowsByDateRangeAndStatus(
            LocalDateTime startDate, LocalDateTime endDate, ShowStatus status);

    Optional<Show> findByIdAndStatus(String id, ShowStatus status);

    boolean existsByScreenIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Long screenId, LocalDateTime endTime, LocalDateTime startTime);

    List<Show> findByStatusAndStartTimeBefore(ShowStatus status, LocalDateTime dateTime);

    long countByMovieIdAndStatus(Long movieId, ShowStatus status);
}
