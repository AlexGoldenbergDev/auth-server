package com.goldenebrg.authserver.services.config;

import lombok.Data;

import java.util.List;

@Data
public class ServerConfig {

    String address;
    int signUpTokenExpirationHours;
    int passwordResetTokenExpirationHours;
    AssignmentsConfiguration assignments;
    List<String> roles;
    int defaultRoleIndex;


}
