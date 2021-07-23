package com.goldenebrg.authserver.security.listener;

import com.goldenebrg.authserver.security.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthFailureListener extends AbstractAuthAttemptListener<AuthenticationFailureBadCredentialsEvent> {


    @Autowired
    AuthFailureListener(HttpServletRequest request, LoginAttemptService loginAttemptService) {
        super(request, loginAttemptService);
    }

    @Override
    void write(String address) {
        loginAttemptService.loginFailed(address);
    }
}