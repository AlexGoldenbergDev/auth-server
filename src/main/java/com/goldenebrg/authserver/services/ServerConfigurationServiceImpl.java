package com.goldenebrg.authserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldenebrg.authserver.services.config.AssignmentJson;
import com.goldenebrg.authserver.services.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Service
public class ServerConfigurationServiceImpl implements ServerConfigurationService {

    private static final String PATH = "/config.json";


    private String host;

    ServerConfig serverConfig;

    @PostConstruct
    void initialize() throws IllegalAccessException {

        this.host = InetAddress.getLoopbackAddress().getHostName();

        try (InputStream stream = AssignmentsServiceImpl.class.getResourceAsStream(PATH)){
            serverConfig = new ObjectMapper().readValue(stream, ServerConfig.class);
        } catch (IOException e) {
            log.error("Exception occurred during loading server config.", e);
            throw new IllegalAccessException("Configure " + PATH + " and then try to restart server");
        }

        validation();
    }

    private void validation() {
        userTokensExpirationValidation();
    }


    private void userTokensExpirationValidation() {
        userTokenExpirationValidation(serverConfig::getPasswordResetTokenExpirationHours, "passwordResetTokenExpirationHours");
        userTokenExpirationValidation(serverConfig::getSignUpTokenExpirationHours, "signUpTokenExpirationHours");
    }


    private void userTokenExpirationValidation(Supplier<Integer> supplier, String name) {
        int hours = supplier.get();
        if (hours < 1) throw new IllegalArgumentException(String.format("Property '%s' value must be >= 1. Your actual value is '%d'", name, hours));
    }


    @Override
    public Map<String, AssignmentJson> getAssignments() {
        return serverConfig.getAssignments().getAssignments();
    }

    @Override
    public List<String> getRoles() {
        return serverConfig.getRoles();
    }

    @Override
    public String getDefaultRole() {
        return serverConfig.getRoles().get(serverConfig.getDefaultRoleIndex());
    }

    @Override
    public int getSignUpTokenExpirationHours() {
        return serverConfig.getSignUpTokenExpirationHours();
    }

    @Override
    public int getPasswordResetTokenExpirationHours() {
        return serverConfig.getPasswordResetTokenExpirationHours();
    }

    @Override
    public String getHost() {
        return Optional.ofNullable(serverConfig.getAddress()).orElse(host);
    }
}
