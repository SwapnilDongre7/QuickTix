package com.quicktix.catalogue.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quicktix.catalogue.dto.CityResponseDTO;
import com.quicktix.catalogue.dto.CreateCityRequest;
import com.quicktix.catalogue.entity.City;
import com.quicktix.catalogue.exception.BadRequestException;
import com.quicktix.catalogue.exception.ResourceNotFoundException;
import com.quicktix.catalogue.repository.CityRepository;
import com.quicktix.catalogue.service.CityService;

@Service
@Transactional
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    public CityServiceImpl(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    // --------------------------------------------------
    // GET ALL CITIES
    // --------------------------------------------------
    @Override
    public List<CityResponseDTO> getAllCities() {
        return cityRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // --------------------------------------------------
    // GET CITY BY ID
    // --------------------------------------------------
    @Override
    public CityResponseDTO getCityById(Integer id) {
        City city = cityRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));
        return mapToDTO(city);
    }

    // --------------------------------------------------
    // CREATE CITY (ADMIN)
    // --------------------------------------------------
    @Override
    public CityResponseDTO createCity(CreateCityRequest request) {
        City city = new City();
        city.setName(request.getName());
        city.setState(request.getState());
        city.setCountry(request.getCountry());

        city.setIsActive(true);

        return mapToDTO(cityRepository.save(city));
    }

    // --------------------------------------------------
    // UPDATE CITY (ADMIN)
    // --------------------------------------------------
    @Override
    public CityResponseDTO updateCity(Integer id, CreateCityRequest request) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        city.setName(request.getName());
        city.setState(request.getState());
        city.setCountry(request.getCountry());

        return mapToDTO(cityRepository.save(city));
    }

    // --------------------------------------------------
    // DEACTIVATE CITY (ADMIN)
    // --------------------------------------------------
    @Override
    public void deactivateCity(Integer id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        if (Boolean.FALSE.equals(city.getIsActive())) {
            throw new BadRequestException("City already deactivated");
        }

        city.setIsActive(false);
        cityRepository.save(city);
    }

    // --------------------------------------------------
    // MAPPER
    // --------------------------------------------------
    private CityResponseDTO mapToDTO(City city) {
        CityResponseDTO dto = new CityResponseDTO();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setState(city.getState());
        dto.setCountry(city.getCountry());
        return dto;
    }

}
