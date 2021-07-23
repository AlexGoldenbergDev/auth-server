package com.goldenebrg.authserver.security.listener;

import com.goldenebrg.authserver.security.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthSuccessListener extends AbstractAuthAttemptListener<AuthenticationSuccessEvent> {

    @Autowired
    AuthSuccessListener(HttpServletRequest request, LoginAttemptService loginAttemptService) {
        super(request, loginAttemptService);
    }

    @Override
    void write(String address) {
        loginAttemptService.loginSucceeded(address);
    }
}