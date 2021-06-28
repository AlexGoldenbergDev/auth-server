package com.goldenebrg.authserver.mail;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class MailServiceImpl implements MailService{

    @Value("${spring.mail.username}")
    String user;

    @Value("classpath:invitationEmail.html")
    Resource invitationEmail;

    @Value("classpath:passwordResetEmail.html")
    Resource passResetEmail;

    String invitationEmailStr;
    String passResetEmailStr;

    private final JavaMailSender emailSender;

    @Autowired
    MailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
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
    public void sendMessage(String receiver, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(user);
        message.setTo(receiver);
        message.setSubject(subject);
        message.setText(content);
        emailSender.send(message);
    }


}
