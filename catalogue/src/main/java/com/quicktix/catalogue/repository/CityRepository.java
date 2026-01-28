package com.quicktix.catalogue.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quicktix.catalogue.entity.City;

public interface CityRepository extends JpaRepository<City, Integer> {
	List<City> findByIsActiveTrue();
	Optional<City> findByIdAndIsActiveTrue(Integer id);
}
