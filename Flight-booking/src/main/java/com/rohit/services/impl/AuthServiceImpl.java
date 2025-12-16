package com.rohit.services.impl;

import com.rohit.dtos.LoginRequest;
import com.rohit.dtos.LoginResponse;
import com.rohit.dtos.RegistrationRequest;
import com.rohit.dtos.Response;
import com.rohit.entities.Role;
import com.rohit.entities.User;
import com.rohit.enums.AuthMethod;
import com.rohit.exceptions.BadRequestException;
import com.rohit.exceptions.NotFoundException;
import com.rohit.repositories.RoleRepo;
import com.rohit.repositories.UserRepo;
import com.rohit.security.JwtUtils;
import com.rohit.services.AuthService;
import com.rohit.services.EmailNotificationServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepo roleRepo;
    private final EmailNotificationServices emailNotificationServices;

    @Override
    public Response<?> register(RegistrationRequest registrationRequest) {
        log.info("Inside register()");

        if (userRepo.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("Email already exist");
        }

        List<Role> userRoles;

        if (registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()) {
            userRoles = registrationRequest.getRoles().stream()
                    .map(roleName -> roleRepo.findByName(roleName.toUpperCase())
                            .orElseThrow(() -> new NotFoundException("Role" + roleName + "Not Found")))
                    .toList();
        }else {
            Role defaultRole = roleRepo.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("Role CUSTOMER DOES NOT EXIST ") );
            userRoles = List.of(defaultRole);

        }

        User userToSave = new User();
        userToSave.setName(registrationRequest.getName());
        userToSave.setEmail(registrationRequest.getEmail());
        userToSave.setPhoneNumber(registrationRequest.getPhoneNumber());
        userToSave.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        userToSave.setRoles(userRoles);
        userToSave.setCreatedAt(LocalDateTime.now());
        userToSave.setUpdatedAt(LocalDateTime.now());
        userToSave.setProvider(AuthMethod.LOCAL);
        userToSave.setActive(true);

        User savedUser = userRepo.save(userToSave);

        emailNotificationServices.sendWelcomeEmail(savedUser);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User registered successfully")
                .build();

    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        log.info("Inside Inside()");

        User user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(()-> new NotFoundException("Email Not Found") );

        if(!user.isActive()){
            throw new NotFoundException("Account Not Active , Please reach out to Customer Care... ");

        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new BadRequestException("Invalid Credentials");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setRoles(roleNames);

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login Successfully")
                .data(loginResponse)
                .build();

    }

}
