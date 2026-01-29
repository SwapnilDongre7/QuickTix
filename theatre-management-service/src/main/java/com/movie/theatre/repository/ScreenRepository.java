package com.movie.theatre.repository;

import com.movie.theatre.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByTheatre_TheatreId(Long theatreId);

    List<Screen> findByTheatre_TheatreIdAndStatus(Long theatreId, Screen.Status status);
}
