package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetService {

    Optional<PasswordResetToken> get(UUID id);

    PasswordResetToken create(User user);

    void delete(PasswordResetToken id);
}
