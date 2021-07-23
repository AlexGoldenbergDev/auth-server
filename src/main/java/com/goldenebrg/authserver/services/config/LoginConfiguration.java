package com.goldenebrg.authserver.services.config;

import lombok.Data;

import java.util.List;

@Data
public class LoginConfiguration {

    Integer maxAttempts;
    Integer attemptTimeoutMinutes;
    int minSize;
    int maxSize;
    List<ConstrainPattern> patterns;
}
