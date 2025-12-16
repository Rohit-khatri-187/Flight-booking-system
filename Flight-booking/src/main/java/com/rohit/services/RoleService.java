package com.rohit.services;

import com.rohit.dtos.Response;
import com.rohit.dtos.RoleDTO;

import java.util.List;

public interface RoleService {

    Response<?> createRole(RoleDTO roleDTO);
    Response<?> updateRole(RoleDTO roleDTO);
    Response<List<RoleDTO>> getAllRoles();

}
