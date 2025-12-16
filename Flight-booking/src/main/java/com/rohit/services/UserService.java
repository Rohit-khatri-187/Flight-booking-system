package com.rohit.services;

import com.rohit.dtos.Response;
import com.rohit.dtos.UserDTO;
import com.rohit.entities.User;

import java.util.List;

public interface UserService {

    User currentUser();
    Response<?> updateMyAccount(UserDTO userDTO);
    Response<List<UserDTO>> getAllPilots();
    Response<UserDTO> getAccountDetails();

}
