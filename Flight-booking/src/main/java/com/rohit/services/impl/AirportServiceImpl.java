package com.rohit.services.impl;

import com.rohit.dtos.AirportDTO;
import com.rohit.dtos.Response;
import com.rohit.entities.Airport;
import com.rohit.enums.City;
import com.rohit.enums.Country;
import com.rohit.exceptions.BadRequestException;
import com.rohit.exceptions.NotFoundException;
import com.rohit.repositories.AirportRepo;
import com.rohit.services.AirportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AirportServiceImpl implements AirportService {

    private final AirportRepo airportRepo;
    private final ModelMapper modelMapper;

    @Override
    public Response<?> createAirport(AirportDTO airportDTO) {
        log.info("Inside createAirports()");

        Country country = airportDTO.getCountry();
        City city = airportDTO.getCity();

        if(!city.getCountry().equals(country)){
            throw new BadRequestException("City doesn't belongs to the country");
        }

        Airport airport = modelMapper.map(airportDTO,Airport.class);
        airportRepo.save(airport);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Airport Created Successfully")
                .build();

    }

    @Override
    public Response<?> updateAirport(AirportDTO airportDTO) {
        Long id = airportDTO.getId();

        Airport existingAirport = airportRepo.findById(id)
                .orElseThrow(()-> new NotFoundException("Airport Not Found"));

        if(airportDTO.getCity() != null){
            if( !airportDTO.getCity().getCountry().equals(existingAirport.getCountry()) ){
                throw new BadRequestException("City doesn't belong to the country ");
            }
            existingAirport.setCity(airportDTO.getCity());
        }

        if(airportDTO.getName() != null){
            existingAirport.setName(airportDTO.getName());
        }

        if(airportDTO.getIataCode() != null){
            existingAirport.setIataCode(airportDTO.getIataCode());
        }

        airportRepo.save(existingAirport);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Airport updated successfully")
                .build();

    }

    @Override
    public Response<List<AirportDTO>> getAllAirports() {
        List<AirportDTO> airports = airportRepo.findAll().stream()
                .map(airport -> modelMapper.map(airport, AirportDTO.class))
                .toList();

        return Response.<List<AirportDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(airports.isEmpty() ? "No Airports Found" : "Airports Retrieved successfully" )
                .data(airports)
                .build();
    }

    @Override
    public Response<AirportDTO> getAirportById(Long id) {
        Airport airport = airportRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Airport Not Found"));

        AirportDTO airportDTO = modelMapper.map(airport, AirportDTO.class);

        return Response.<AirportDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Airports retrieved successfully")
                .data(airportDTO)
                .build();

    }
}
