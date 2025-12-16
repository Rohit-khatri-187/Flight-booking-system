package com.rohit.repositories;

import com.rohit.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

//    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName ")
//    List<User> findByRoleName(@Param("roleName") String roleName);

    List<User> findByRolesName(String roleName);

}
