package com.goldenebrg.authserver.jpa.dao;

import com.goldenebrg.authserver.jpa.entities.UserServices;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ServicesDao extends JpaRepository<UserServices, UUID> {
}
