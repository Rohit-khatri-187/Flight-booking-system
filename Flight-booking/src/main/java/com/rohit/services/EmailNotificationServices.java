package com.rohit.services;

import com.rohit.entities.Booking;
import com.rohit.entities.User;

public interface EmailNotificationServices {

    void sendBookingTickerEmail(Booking booking);
    void sendWelcomeEmail(User user);

}
