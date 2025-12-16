package com.rohit.services.impl;

import com.rohit.dtos.Response;
import com.rohit.dtos.UserDTO;
import com.rohit.entities.User;
import com.rohit.exceptions.NotFoundException;
import com.rohit.repositories.UserRepo;
import com.rohit.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepo.findByEmail(email)
                .orElseThrow(()-> new NotFoundException("User Not Found"));
    }

    @Override
    @Transactional
    public Response<?> updateMyAccount(UserDTO userDTO) {
       log.info("Inside updateMyAccount()");

       User user = currentUser();

       if(userDTO.getName() != null && !userDTO.getName().isBlank()){
           user.setName(userDTO.getName());
       }

        if(userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().isBlank()){
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }

        if(userDTO.getPassword() != null && !userDTO.getPassword().isBlank()){
            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            user.setPassword(encodedPassword);
        }

        user.setUpdatedAt(LocalDateTime.now());

        userRepo.save(user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account Updated Successfully")
                .build();

    }

    @Override
    public Response<List<UserDTO>> getAllPilots() {
        log.info("Inside getAllPilots()");

        List<UserDTO> pilots = userRepo.findByRolesName("PILOT").stream()
                .map(user -> modelMapper.map(user,UserDTO.class))
                .toList();

        return Response.<List<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(pilots.isEmpty() ? "No Pilot Found" : "Pilots retrieved successfully" )
                .data(pilots)
                .build();

    }

    @Override
    public Response<UserDTO> getAccountDetails() {
        log.info("inside getAccountDetails() ");

        User user = currentUser();

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(userDTO)
                .build();

    }
}
