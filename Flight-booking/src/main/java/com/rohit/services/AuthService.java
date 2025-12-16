package com.rohit.services;

import com.rohit.dtos.LoginRequest;
import com.rohit.dtos.LoginResponse;
import com.rohit.dtos.RegistrationRequest;
import com.rohit.dtos.Response;

public interface AuthService {

    Response<?> register(RegistrationRequest registrationRequest);
    Response<LoginResponse> login(LoginRequest loginRequest);

}
