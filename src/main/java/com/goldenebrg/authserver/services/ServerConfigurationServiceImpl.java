package com.goldenebrg.authserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldenebrg.authserver.services.config.AssignmentJson;
import com.goldenebrg.authserver.services.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ServerConfigurationServiceImpl implements ServerConfigurationService {

    private static final String PATH = "/config.json";

    ServerConfig serverConfig;

    @PostConstruct
    void initialize() throws IllegalAccessException {

        try (InputStream stream = AssignmentsServiceImpl.class.getResourceAsStream(PATH)){
            serverConfig = new ObjectMapper().readValue(stream, ServerConfig.class);
        } catch (IOException e) {
            log.error("Exception occurred during loading server config.", e);
            throw new IllegalAccessException("Configure " + PATH + " and then try to restart server");
        }
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
}
