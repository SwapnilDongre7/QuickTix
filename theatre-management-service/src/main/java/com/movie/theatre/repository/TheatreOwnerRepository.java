package com.movie.theatre.repository;

import com.movie.theatre.entity.TheatreOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TheatreOwnerRepository extends JpaRepository<TheatreOwner, Long> {

    Optional<TheatreOwner> findByUserId(Long userId);
}
