package com.quicktix.catalogue.service;

import java.util.List;

import com.quicktix.catalogue.dto.CityResponseDTO;
import com.quicktix.catalogue.dto.CreateCityRequest;


public interface CityService {

	List<CityResponseDTO > getAllCities();

	CityResponseDTO  getCityById(Integer id);

	CityResponseDTO  createCity(CreateCityRequest request);

	void deactivateCity(Integer id);
}
