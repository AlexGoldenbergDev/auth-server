package com.goldenebrg.authserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldenebrg.authserver.services.config.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServerConfigurationServiceImpl implements ServerConfigurationService {

    private static final String PATH = "/config.json";


    private String host;

    AppConfig appConfig;

    @PostConstruct
    void initialize() throws IllegalAccessException {

        this.host = InetAddress.getLoopbackAddress().getHostName();

        try (InputStream stream = ServerConfigurationService.class.getResourceAsStream(PATH)) {
            appConfig = new ObjectMapper().readValue(stream, AppConfig.class);

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
        userTokenExpirationValidation(appConfig::getPasswordResetTokenExpirationHours, "passwordResetTokenExpirationHours");
        userTokenExpirationValidation(appConfig::getSignUpTokenExpirationHours, "signUpTokenExpirationHours");
    }


    private void userTokenExpirationValidation(Supplier<Integer> supplier, String name) {
        int hours = supplier.get();
        if (hours < 1) throw new IllegalArgumentException(String.format("Property '%s' value must be >= 1. Your actual value is '%d'", name, hours));
    }


    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public Map<String, ServiceJson> getServices() {
        return appConfig.getServices().getServices();
    }

    @Override
    @NotNull
    @NotEmpty
    @Cacheable(value = "config", key = "#root.method.name")
    public List<String> getRoles() {
        return appConfig.getRoles();
    }

    @Override
    @NotNull
    @NotEmpty
    @Cacheable(value = "config", key = "#root.method.name")

    public String getDefaultRole() {
        return appConfig.getRoles().get(appConfig.getDefaultRoleIndex());
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public int getSignUpTokenExpirationHours() {
        return appConfig.getSignUpTokenExpirationHours();
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public int getPasswordResetTokenExpirationHours() {
        return appConfig.getPasswordResetTokenExpirationHours();
    }

    @Override
    @NotNull
    @NotEmpty
    @Cacheable(value = "config", key = "#root.method.name")
    public String getHost() {
        return Optional.ofNullable(appConfig.getServer()).map(ServerConfiguration::getAddress).orElse(host);
    }

    @Override
    @NotNull
    @Cacheable(value = "config", key = "#root.method.name")
    public List<ConstrainPattern> getPasswordPatterns() {
        return Optional.ofNullable(appConfig.getSecurity())
                .map(SecurityConfiguration::getPassword)
                .map(PasswordConfiguration::getPatterns)
                .orElse(Collections.emptyList());
    }

    @Override
    @NotNull
    @Cacheable(value = "config", key = "#root.method.name")
    public List<ConstrainPattern> getLoginPatterns() {
        return Optional.ofNullable(appConfig.getSecurity())
                .map(SecurityConfiguration::getLogin)
                .map(LoginConfiguration::getPatterns)
                .orElse(Collections.emptyList());
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public int getPasswordMinSize() {
        return Optional.ofNullable(appConfig.getSecurity())
                .map(SecurityConfiguration::getPassword)
                .map(PasswordConfiguration::getMinSize)
                .filter(size -> size != 0).orElse(6);
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public int getPasswordMaxSize() {
        return Optional.ofNullable(appConfig.getSecurity())
                .map(SecurityConfiguration::getPassword)
                .map(PasswordConfiguration::getMinSize)
                .filter(size -> size != 0).orElse(32);
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public int getLoginMinSize() {
        return Optional.ofNullable(appConfig.getSecurity())
                .map(SecurityConfiguration::getLogin)
                .map(LoginConfiguration::getMinSize)
                .filter(size -> size != 0).orElse(3);
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public int getLoginMaxAttempts() {
        return Optional.ofNullable(appConfig.getSecurity())
                .map(SecurityConfiguration::getLogin)
                .map(LoginConfiguration::getMaxAttempts).orElse(3);
    }


    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public int getLoginAttemptTimeoutMinutes() {
        return Optional.ofNullable(appConfig.getSecurity())
                .map(SecurityConfiguration::getLogin)
                .map(LoginConfiguration::getAttemptTimeoutMinutes).orElse(5);
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public int getLoginMaxSize() {
        return Optional.ofNullable(appConfig.getSecurity())
                .map(SecurityConfiguration::getLogin)
                .map(LoginConfiguration::getMinSize)
                .filter(size -> size != 0).orElse(24);
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public List<String> getCorsOrigins() {
        return Optional.ofNullable(appConfig.getServer())
                .map(ServerConfiguration::getCors)
                .map(CorsConfiguration::getOrigins)
                .orElse(Collections.singletonList("localhost"));
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public List<String> getCorsMethods() {
        return Optional.ofNullable(appConfig.getServer())
                .map(ServerConfiguration::getCors)
                .map(CorsConfiguration::getMethods)
                .orElse(Collections.emptyList());
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public List<String> getCorsHeaders() {
        return Optional.ofNullable(appConfig.getServer())
                .map(ServerConfiguration::getCors)
                .map(CorsConfiguration::getHeaders)
                .orElse(Collections.emptyList());
    }

    @Override
    @Cacheable(value = "servicesChangers", key = "#service")
    public Set<String> getServicesChangers(String service) {

        return Optional.ofNullable(getServices().get(service)).map(ServiceJson::getChangers).orElse(Collections.emptySet());
    }

    @Override
    @Cacheable(value = "config", key = "#root.method.name")
    public String getAdminRole() {
        int adminRoleIndex = appConfig.getAdminRoleIndex();
        return appConfig.getRoles().get(adminRoleIndex);
    }

    @Override
    public Set<String> getServicesNames(String role) {
        return getServices().entrySet().stream()
                .filter(entry -> entry.getValue().getChangers().contains(role))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    @Cacheable(value = "serviceExistence", key = "#service")
    public boolean isServiceExists(String service) {
        return getServices().containsKey(service);
    }

    @Override
    public boolean isServiceNotExists(String service) {
        return !isServiceExists(service);
    }

    @Override
    @Cacheable(value = "servicesInputFields", key = "#service")
    public Map<String, ServiceInputField> getServicesInputFieldsMap(String service) {
        if (isServiceNotExists(service)) return Collections.emptyMap();

        return Optional.ofNullable(getServices().get(service).getInputs())
                .map(fields -> fields.stream().collect(Collectors.toMap(ServiceInputField::getName, field -> field)))
                .orElse(Collections.emptyMap());
    }

    @Override
    @Cacheable(value = "servicesSelectionFields", key = "#service")
    public Map<String, ServiceSelectionListField> getServicesSelectionListFieldsMap(String service) {
        if (isServiceNotExists(service)) return Collections.emptyMap();
        return Optional.ofNullable(getServices().get(service).getLists())
                .map(fields -> fields.stream().collect(Collectors.toMap(ServiceSelectionListField::getName, field -> field)))
                .orElse(Collections.emptyMap());
    }


}
