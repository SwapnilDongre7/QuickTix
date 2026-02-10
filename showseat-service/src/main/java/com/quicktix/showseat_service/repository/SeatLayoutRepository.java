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

    /**
     * Find all layouts created by a specific owner (for reusable templates)
     */
    List<SeatLayout> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /**
     * Find active layouts created by a specific owner
     */
    List<SeatLayout> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(Long createdBy);

    /**
     * Find all active layouts (for fallback/admin views)
     */
    List<SeatLayout> findByIsActiveTrueOrderByCreatedAtDesc();
}
//
//package com.quicktix.showseat_service.repository;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import com.quicktix.showseat_service.model.document.SeatLayout;
//
//@Repository
//public interface SeatLayoutRepository extends MongoRepository<SeatLayout, String> {
//
//    List<SeatLayout> findByScreenIdAndIsActiveTrue(Long screenId);
//
//    Optional<SeatLayout> findByIdAndIsActiveTrue(String id);
//
//    List<SeatLayout> findByScreenIdOrderByVersionDesc(Long screenId);
//
//    Optional<SeatLayout> findTopByScreenIdOrderByVersionDesc(Long screenId);
//
//    boolean existsByScreenIdAndLayoutNameAndIsActiveTrue(Long screenId, String layoutName);
//
//    long countByScreenId(Long screenId);
//
//    /**
//     * Find all layouts created by a specific owner (for reusable templates)
//     */
//    List<SeatLayout> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
//
//    /**
//     * Find active layouts created by a specific owner
//     */
//    List<SeatLayout> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(Long createdBy);
//
//    /**
//     * Find all active layouts (for fallback/admin views)
//     */
//    List<SeatLayout> findByIsActiveTrueOrderByCreatedAtDesc();
//}