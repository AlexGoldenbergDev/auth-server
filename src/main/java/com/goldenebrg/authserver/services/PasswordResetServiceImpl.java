package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.PasswordResetDao;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.mail.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.goldenebrg.authserver.services.CrossServiceUtils.createUniqueUUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetDao passwordResetDao;
    private final MailService mailService;
    private final UserService userService;

    @Autowired
    public PasswordResetServiceImpl(MailService mailService, PasswordResetDao passwordResetDao, UserService userService) {
        this.passwordResetDao = passwordResetDao;
        this.mailService = mailService;
        this.userService = userService;
    }

    @Override
    public Optional<PasswordResetToken> get(UUID id) {
        return passwordResetDao.findById(id);
    }

    @Override
    public PasswordResetToken create(User user) {
        UUID uniqueUUID = createUniqueUUID(passwordResetDao);
        PasswordResetToken passwordResetToken = new PasswordResetToken(uniqueUUID, new Date(), user.getEmail());
        mailService.sendPasswordResetEmail(passwordResetToken);
        passwordResetDao.save(passwordResetToken);
        return passwordResetToken;
    }

    @Override
    public void delete(PasswordResetToken passwordResetToken) {
        passwordResetDao.delete(passwordResetToken);
    }
}
