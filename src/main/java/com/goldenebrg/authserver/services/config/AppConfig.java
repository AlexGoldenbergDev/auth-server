package com.goldenebrg.authserver.services.config;

import lombok.Data;

import java.util.List;

@Data
public class AppConfig {

    ServerConfiguration server;
    int signUpTokenExpirationHours;
    int passwordResetTokenExpirationHours;
    AssignmentsConfiguration assignments;
    SecurityConfiguration security;
    List<String> roles;
    int defaultRoleIndex;


}
