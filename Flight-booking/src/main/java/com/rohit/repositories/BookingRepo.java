package com.rohit.repositories;

import com.rohit.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepo  extends JpaRepository<Booking,Long> {

    List<Booking> findByUserIdOrderByIdDesc(Long userId);

}
