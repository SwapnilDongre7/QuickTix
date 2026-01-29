package com.movie.theatre.repository;

import com.movie.theatre.entity.TheatreOwner;
import com.movie.theatre.entity.TheatreOwner.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TheatreOwnerRepository extends JpaRepository<TheatreOwner, Long> {

    Optional<TheatreOwner> findByUserId(Long userId);

    List<TheatreOwner> findByStatus(ApplicationStatus status);
}
