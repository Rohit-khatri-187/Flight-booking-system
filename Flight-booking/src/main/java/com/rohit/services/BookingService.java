package com.rohit.services;

import com.rohit.dtos.BookingDTO;
import com.rohit.dtos.CreateBookingRequest;
import com.rohit.dtos.Response;
import com.rohit.enums.BookingStatus;

import java.util.List;

public interface BookingService {

    Response<?> createBooking(CreateBookingRequest createBookingRequest);
    Response<BookingDTO> getBookingById(Long id);
    Response<List<BookingDTO>> getAllBookings();
    Response<List<BookingDTO>> getMyBookings();
    Response<?> updateBookingStatus(Long id, BookingStatus status);

    //2:07

}
