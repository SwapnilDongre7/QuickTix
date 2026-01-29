package com.quicktix.showseat_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.quicktix.showseat_service.model.document.SeatLayout;

@Repository
public interface SeatLayoutRepository extends MongoRepository<SeatLayout, String> {

    List<SeatLayout> findByScreenIdAndIsActiveTrue(Long screenId);

    Optional<SeatLayout> findByIdAndIsActiveTrue(String id);

    List<SeatLayout> findByScreenIdOrderByVersionDesc(Long screenId);

    Optional<SeatLayout> findTopByScreenIdOrderByVersionDesc(Long screenId);

    boolean existsByScreenIdAndLayoutNameAndIsActiveTrue(Long screenId, String layoutName);

    long countByScreenId(Long screenId);
}