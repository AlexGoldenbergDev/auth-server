package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.services.config.AssignmentJson;
import com.goldenebrg.authserver.services.config.ConstrainPattern;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ServerConfigurationService {

    Map<String, AssignmentJson> getAssignments();

    @NotNull @NotEmpty List<String> getRoles();

    @NotNull @NotEmpty String getDefaultRole();

    int getSignUpTokenExpirationHours();

    int getPasswordResetTokenExpirationHours();

    @NotNull @NotEmpty String getHost();

    @NotNull List<ConstrainPattern> getPasswordPatterns();

    @NotNull List<ConstrainPattern> getLoginPatterns();

    int getPasswordMinSize();

    int getPasswordMaxSize();

    int getLoginMinSize();

    int getLoginMaxAttempts();

    int getLoginAttemptTimeoutMinutes();

    int getLoginMaxSize();

    List<String> getCorsOrigins();

    List<String> getCorsMethods();

    List<String> getCorsHeaders();

    Set<String> getAssignmentChangers(String assignment);

    String getAdminRole();
}
