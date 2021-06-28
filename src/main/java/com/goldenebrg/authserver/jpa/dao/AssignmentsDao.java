package com.goldenebrg.authserver.jpa.dao;

import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssignmentsDao extends JpaRepository<UserAssignments, UUID> {
}
