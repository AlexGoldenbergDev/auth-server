package com.goldenebrg.authserver.mail;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;

import javax.mail.MessagingException;

public interface MailService {
    void sendMessage(String receiver, String subject, String content) throws MessagingException;

    void sendSignUpRequest(InvitationToken invitationToken);

    void sendPasswordResetEmail(PasswordResetToken passwordResetToken);
}
