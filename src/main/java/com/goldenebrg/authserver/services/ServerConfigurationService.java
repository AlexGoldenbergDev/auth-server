package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.services.config.AssignmentJson;

import java.util.List;
import java.util.Map;

public interface ServerConfigurationService {
    Map<String, AssignmentJson> getAssignments();

    List<String> getRoles();

    String getDefaultRole();
}
