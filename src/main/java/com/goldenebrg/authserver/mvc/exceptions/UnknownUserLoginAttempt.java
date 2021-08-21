package com.goldenebrg.authserver.mvc.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnknownUserLoginAttempt extends RuntimeException {

    public UnknownUserLoginAttempt() {
        super("Unrecognizable user login attempt.");
    }
}
