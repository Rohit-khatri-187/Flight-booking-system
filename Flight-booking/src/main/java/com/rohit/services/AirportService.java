package com.rohit.services;

import com.rohit.dtos.AirportDTO;
import com.rohit.dtos.Response;

import java.util.List;

public interface AirportService {

    Response<?> createAirport(AirportDTO airportDTO);
    Response<?> updateAirport(AirportDTO airportDTO);
    Response<List<AirportDTO>> getAllAirports();
    Response<AirportDTO> getAirportById(Long id);

}
