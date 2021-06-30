package com.goldenebrg.authserver.mail;


import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class MailServiceImpl implements MailService{

    private static final String EXPIRATION_PLACEHOLDER = "::expiration";
    private static final String HOST_PLACEHOLDER = "::host";
    private static final String LINK_PLACEHOLDER = "::link";

    private final ServerConfigurationService serverConfigurationService;
    private final JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    String user;

    @Value("classpath:invitationEmail.html")
    Resource invitationEmail;

    @Value("classpath:passwordResetEmail.html")
    Resource passResetEmail;

    String invitationEmailStr;
    String passResetEmailStr;


    @Autowired
    MailServiceImpl(JavaMailSender emailSender, ServerConfigurationService serverConfigurationService) {
        this.emailSender = emailSender;
        this.serverConfigurationService = serverConfigurationService;
    }

    @PostConstruct
    void initialize() {
        this.invitationEmailStr = loadMessageResource(invitationEmail);
        this.passResetEmailStr = loadMessageResource(passResetEmail);
    }

    private String loadMessageResource(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read Email Templates HTML text", e);
        }
    }

    @Override
    public void sendMessage(String receiver, String subject, String content) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, UTF_8.displayName());
        helper.setFrom(user);
        helper.setTo(receiver);
        message.setSubject(subject);
        message.setContent(content, "text/html");
        emailSender.send(message);
    }


    @Override
    public void sendSignUpRequest(InvitationToken invitationToken) {
        String host = serverConfigurationService.getHost();
        int expiration = serverConfigurationService.getSignUpTokenExpirationHours();
        String content = this.invitationEmailStr;

        content = content.replaceAll(EXPIRATION_PLACEHOLDER, String.valueOf(expiration));
        content = content.replaceAll(HOST_PLACEHOLDER, host);
        content = content.replaceAll(LINK_PLACEHOLDER, String.format("%s/signup/%s", host,
                invitationToken.getId().toString()));
        try {
            sendMessage(invitationToken.getEmail(), "Sign Up Invitation", content);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPasswordResetEmail(PasswordResetToken passwordResetToken) {
        String host = serverConfigurationService.getHost();
        int expiration = serverConfigurationService.getPasswordResetTokenExpirationHours();
        String content = this.passResetEmailStr;

        content = content.replaceAll(EXPIRATION_PLACEHOLDER, String.valueOf(expiration));
        content = content.replaceAll(HOST_PLACEHOLDER, host);
        content = content.replaceAll(LINK_PLACEHOLDER, String.format("%s/reset/%s", host,
                passwordResetToken.getId().toString()));
        try {
            sendMessage(passwordResetToken.getEmail(), "Password Reset", content);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }


}
