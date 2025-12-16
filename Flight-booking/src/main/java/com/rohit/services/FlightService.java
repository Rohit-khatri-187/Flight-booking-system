package com.rohit.services;

import com.rohit.dtos.CreateFlightRequest;
import com.rohit.dtos.FlightDTO;
import com.rohit.dtos.Response;
import com.rohit.enums.City;
import com.rohit.enums.Country;
import com.rohit.enums.FlightStatus;

import java.time.LocalDate;
import java.util.List;

public interface FlightService {

    Response<?> createFlight(CreateFlightRequest createFlightRequest);
    Response<FlightDTO> getFlightById(Long id);
    Response<List<FlightDTO>> getAllFlights();
    Response<?> updateFlight(CreateFlightRequest createFlightRequest);
    Response<List<FlightDTO>> searchFlight(String departurePortIata , String arrivalPortIata , FlightStatus status, LocalDate departureDate);
    Response<List<City>> getAllCities();
    Response<List<Country>> getAllCountries();

}
