package com.goldenebrg.authserver.security.listener;

import com.goldenebrg.authserver.security.LoginAttemptService;
import com.goldenebrg.authserver.security.auth.service.UserDetailsServiceImpl;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;

import javax.servlet.http.HttpServletRequest;

abstract class AbstractAuthAttemptListener<T extends AbstractAuthenticationEvent>
        implements ApplicationListener<T> {

    final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;

    AbstractAuthAttemptListener(HttpServletRequest request, LoginAttemptService loginAttemptService) {
        this.request = request;
        this.loginAttemptService = loginAttemptService;
    }

    abstract void write(String address);

    @Override

    public void onApplicationEvent(T event) {
        write(UserDetailsServiceImpl.getClientIP(request));
    }
}