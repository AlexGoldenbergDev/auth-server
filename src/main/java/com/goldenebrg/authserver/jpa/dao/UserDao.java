package com.goldenebrg.authserver.jpa.dao;

import com.goldenebrg.authserver.jpa.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserDao extends JpaRepository<User, UUID> {

    User findUserByUsername(String username);
}
