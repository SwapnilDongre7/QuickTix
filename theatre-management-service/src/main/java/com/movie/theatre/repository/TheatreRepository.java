package com.movie.theatre.repository;

import com.movie.theatre.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    List<Theatre> findByOwner_Id(Long ownerId);

    List<Theatre> findByCityId(Integer cityId);
    
    List<Theatre> findByCityIdAndStatus(Integer cityId, Theatre.Status status);

    List<Theatre> findByOwner_IdAndStatus(Long ownerId, Theatre.Status status);
}
