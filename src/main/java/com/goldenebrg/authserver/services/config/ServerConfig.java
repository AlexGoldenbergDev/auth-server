package com.goldenebrg.authserver.services.config;

import lombok.Data;

import java.util.List;

@Data
public class ServerConfig {

    AssignmentsConfiguration assignments;
    List<String> roles;
    int defaultRoleIndex;


}
