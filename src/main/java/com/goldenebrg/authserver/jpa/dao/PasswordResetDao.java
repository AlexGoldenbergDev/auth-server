package com.goldenebrg.authserver.jpa.dao;

import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.UUID;

public interface PasswordResetDao extends JpaRepository<PasswordResetToken, UUID> {

    void deleteAllByCreationDateIsBetween(Date date0, Date date1);
}
