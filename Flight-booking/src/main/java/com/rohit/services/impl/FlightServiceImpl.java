package com.rohit.services.impl;

import com.rohit.dtos.CreateFlightRequest;
import com.rohit.dtos.FlightDTO;
import com.rohit.dtos.Response;
import com.rohit.entities.Airport;
import com.rohit.entities.Flight;
import com.rohit.entities.User;
import com.rohit.enums.City;
import com.rohit.enums.Country;
import com.rohit.enums.FlightStatus;
import com.rohit.exceptions.BadRequestException;
import com.rohit.exceptions.NotFoundException;
import com.rohit.repositories.AirportRepo;
import com.rohit.repositories.FlightRepo;
import com.rohit.repositories.UserRepo;
import com.rohit.services.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepo flightRepo;
    private final AirportRepo airportRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;

    @Override
    public Response<?> createFlight(CreateFlightRequest createFlightRequest) {

        if(createFlightRequest.getArrivalTime().isBefore(createFlightRequest.getDepartureTime())){
            throw new BadRequestException("Arrival Time cannot be before the departure time");
        }

        if(flightRepo.existsByFlightNumber(createFlightRequest.getFlightNumber())){
            throw new BadRequestException("Flight with this number already exists");
        }

        // fetch and validate the departure airports

        Airport departureAirport = airportRepo.findByIataCode(createFlightRequest.getDepartureAirportIataCode())
                .orElseThrow(()-> new NotFoundException("Departure Airport Not Found"));

        Airport arrivalAirport = airportRepo.findByIataCode(createFlightRequest.getArrivalAirportIataCode())
                .orElseThrow(()-> new NotFoundException("Arrival Airports Not Found"));

        Flight flightToSave = new Flight();
        flightToSave.setFlightNumber(createFlightRequest.getFlightNumber());
        flightToSave.setDepartureAirport(departureAirport);
        flightToSave.setArrivalAirport(arrivalAirport);
        flightToSave.setDepartureTime(createFlightRequest.getDepartureTime());
        flightToSave.setArrivalTime(createFlightRequest.getArrivalTime());
        flightToSave.setBasePrice(createFlightRequest.getBasePrice());
        flightToSave.setStatus(FlightStatus.SCHEDULED);

        //assign pilot to the flights

        if(createFlightRequest.getPilotId() != null){
            User pilot = userRepo.findById(createFlightRequest.getPilotId())
                    .orElseThrow(()-> new NotFoundException("Pilot is not found"));

            boolean isPilot = pilot.getRoles().stream()
                    .anyMatch(role -> role.getName().equalsIgnoreCase("PILOT") );

            if(!isPilot){
                throw new BadRequestException("Claimed User Pilot not a certified pilot");
            }
            flightToSave.setAssignedPilot(pilot);
        }


        flightRepo.save(flightToSave);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Flight saved successfully")
                .build();
    }

    @Override
    public Response<FlightDTO> getFlightById(Long id) {

        Flight flight = flightRepo.findById(id)
                .orElseThrow(()-> new NotFoundException("Flight Not Found"));

        FlightDTO flightDTO = modelMapper.map(flight, FlightDTO.class);

        if(flightDTO.getBookings() != null){
            flightDTO.getBookings().forEach(bookingDTO -> bookingDTO.setFlight(null));
        }

        return Response.<FlightDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Flight Retrieved successfully")
                .data(flightDTO)
                .build();
    }

    @Override
    public Response<List<FlightDTO>> getAllFlights() {

        Sort sortByIdDesc = Sort.by(Sort.Direction.DESC,"id");
        List<FlightDTO> flights = flightRepo.findAll(sortByIdDesc).stream()
                .map(flight -> {
                    FlightDTO flightDTO = modelMapper.map(flight, FlightDTO.class);

                    if(flightDTO.getBookings() != null){
                        flightDTO.getBookings().forEach(bookingDTO -> bookingDTO.setFlight(null));
                    }
                    return flightDTO;
                }).toList();

        return Response.<List<FlightDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(flights.isEmpty() ? "No Flights Found" : "Flights retrieved successfully")
                .data(flights)
                .build();
    }


    @Override
    @Transactional
    public Response<?> updateFlight(CreateFlightRequest flightRequest) {

        Long id = flightRequest.getId();

        Flight existingFlight = flightRepo.findById(id)
                .orElseThrow(()-> new NotFoundException("Flight Not Found"));

        if(flightRequest.getDepartureTime() != null){
            existingFlight.setDepartureTime(flightRequest.getDepartureTime());
        }

        if(flightRequest.getArrivalTime() != null){
            existingFlight.setArrivalTime(flightRequest.getArrivalTime());
        }

        if(flightRequest.getBasePrice() != null){
            existingFlight.setBasePrice(flightRequest.getBasePrice());
        }

        if(flightRequest.getStatus() != null){
            existingFlight.setStatus(flightRequest.getStatus());
        }

        // if pilot id is passed in validate the pilot and update it
        if(flightRequest.getPilotId() != null){
            User pilot = userRepo.findById(flightRequest.getPilotId())
                    .orElseThrow(()-> new NotFoundException("Pilot is not found"));

            boolean isPilot = pilot.getRoles().stream()
                    .anyMatch(role -> role.getName().equalsIgnoreCase("PILOT") );

            if(!isPilot){
                throw new BadRequestException("Claimed User Pilot not a certified pilot");
            }
            existingFlight.setAssignedPilot(pilot);
        }

        flightRepo.save(existingFlight);
        //26 min

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Flight updated successfully")
                .build();
    }

    @Override
    public Response<List<FlightDTO>> searchFlight(String departurePortIata, String arrivalPortIata, FlightStatus status, LocalDate departureDate) {

        LocalDateTime startOfDay = departureDate.atStartOfDay();
        LocalDateTime endOfDay = departureDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<Flight> flights = flightRepo.findByDepartureAirportIataCodeAndArrivalAirportIataCodeAndStatusAndDepartureTimeBetween(
                departurePortIata,arrivalPortIata,status,startOfDay,endOfDay);

        List<FlightDTO> flightDTOS = flights.stream()
                .map(flight -> {
                    FlightDTO flightDTO = modelMapper.map(flight, FlightDTO.class);
                    flightDTO.setAssignedPilot(null);
                    flightDTO.setBookings(null);

                    return flightDTO;
                }).toList();

        return Response.<List<FlightDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(flightDTOS.isEmpty() ? "No Flights Found" : "Flight retrieved successfully" )
                .data(flightDTOS)
                .build();
    }

    @Override
    public Response<List<City>> getAllCities() {
        return Response.<List<City>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success")
                .data(List.of(City.values()))
                .build();
    }

    @Override
    public Response<List<Country>> getAllCountries() {
        return Response.<List<Country>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success")
                .data(List.of(Country.values()))
                .build();
    }
}
