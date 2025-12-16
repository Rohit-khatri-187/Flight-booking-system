package com.rohit.services.impl;

import com.rohit.dtos.BookingDTO;
import com.rohit.dtos.CreateBookingRequest;
import com.rohit.dtos.Response;
import com.rohit.entities.Booking;
import com.rohit.entities.Flight;
import com.rohit.entities.Passenger;
import com.rohit.entities.User;
import com.rohit.enums.BookingStatus;
import com.rohit.enums.FlightStatus;
import com.rohit.exceptions.BadRequestException;
import com.rohit.exceptions.NotFoundException;
import com.rohit.repositories.BookingRepo;
import com.rohit.repositories.FlightRepo;
import com.rohit.repositories.PassengerRepo;
import com.rohit.services.BookingService;
import com.rohit.services.EmailNotificationServices;
import com.rohit.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepo bookingRepo;
    private final UserService userService;
    private final FlightRepo flightRepo;
    private final PassengerRepo passengerRepo;
    private final ModelMapper modelMapper;
    private final EmailNotificationServices emailNotificationServices;


    @Override
    @Transactional
    public Response<?> createBooking(CreateBookingRequest createBookingRequest) {

        User user = userService.currentUser();

        Flight flight = flightRepo.findById(createBookingRequest.getFlightId())
                .orElseThrow(()-> new NotFoundException("Flight not found"));

        if(flight.getStatus() != FlightStatus.SCHEDULED){
            throw new BadRequestException("You can only book a flight that is scheduled");
        }

        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setUser(user);
        booking.setFlight(flight);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepo.save(booking);

        if(createBookingRequest.getPassengers() != null && !createBookingRequest.getPassengers().isEmpty()){
            List<Passenger> passengers = createBookingRequest.getPassengers().stream()
                    .map(passengerDTO -> {
                        Passenger passenger = modelMapper.map(passengerDTO, Passenger.class);
                        passenger.setBooking(savedBooking);
                        return passenger;
                    }).toList();
            passengerRepo.saveAll(passengers);
            savedBooking.setPassengers(passengers);
        }

        //send email ticket out
        emailNotificationServices.sendBookingTickerEmail(savedBooking);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Booking created successfully")
                .build();
    }

    @Override
    public Response<BookingDTO> getBookingById(Long id) {

        Booking booking = bookingRepo.findById(id)
                .orElseThrow(()-> new NotFoundException("booking not found"));

        BookingDTO bookingDTO = modelMapper.map(booking, BookingDTO.class);
        bookingDTO.getFlight().setBookings(null);


        return Response.<BookingDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Booking retrieved successfully")
                .data(bookingDTO)
                .build();
    }

    @Override
    public Response<List<BookingDTO>> getAllBookings() {

        List<Booking> allBookings = bookingRepo.findAll(Sort.by(Sort.Direction.DESC,"id"));

        List<BookingDTO> bookings = allBookings.stream()
                .map(booking -> {
                    BookingDTO bookingDTO = modelMapper.map(booking, BookingDTO.class);
                    bookingDTO.getFlight().setBookings(null);
                    return bookingDTO;
                }).toList();

        return Response.<List<BookingDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(bookings.isEmpty() ? "No Booking Found" :"Booking retrieved successfully")
                .data(bookings)
                .build();
    }

    @Override
    public Response<List<BookingDTO>> getMyBookings() {

        User user = userService.currentUser();
        List<Booking> userBookings = bookingRepo.findByUserIdOrderByIdDesc(user.getId());

        List<BookingDTO> bookings = userBookings.stream()
                .map(booking -> {
                    BookingDTO bookingDTO = modelMapper.map(booking, BookingDTO.class);
                    bookingDTO.getFlight().setBookings(null);

                    return bookingDTO;
                }).toList();

        return Response.<List<BookingDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(bookings.isEmpty() ? "No booking found for this use ": "user Bookings retrieved successfully ")
                .data(bookings)
                .build();
    }

    @Override
    @Transactional
    public Response<?> updateBookingStatus(Long id, BookingStatus status) {
        log.info("Inside the update booking status "+ id+" "+ status);
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(()-> new NotFoundException("Booking Not Found") );

        booking.setStatus(status);
        bookingRepo.save(booking);


        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Booking Updated Successfully")
                .build();

    }

    //implement to make sure the booking reference doesn't exist
    private String generateBookingReference(){
        return UUID.randomUUID().toString().substring(0,8).toUpperCase();
    }

}
