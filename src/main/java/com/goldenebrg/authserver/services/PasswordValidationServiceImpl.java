package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.rest.beans.PasswordInputForm;
import com.goldenebrg.authserver.services.config.ConstrainPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PasswordValidationServiceImpl implements PasswordValidationService {

    private final ServerConfigurationService configurationService;

    @Autowired
    public PasswordValidationServiceImpl(ServerConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public List<String> validate(PasswordInputForm dto) {
        String password = dto.getPassword();
        String matchingPassword = dto.getMatchingPassword();

        List<String> messages = new LinkedList<>();

        if (!password.equals(matchingPassword))
            messages.add("Passwords doesn't match each other");

        int passwordMaxSize = configurationService.getPasswordMaxSize();
        int passwordMinSize = configurationService.getPasswordMinSize();
        if (password.length() > passwordMaxSize || password.length() < passwordMinSize)
            messages.add(String.format("Passwords size must be between %d and %d characters", passwordMinSize, passwordMaxSize));

        configurationService.getPasswordPatterns().stream().filter(ptn -> !Pattern.compile(ptn.getPattern()).matcher(password).find())
                .map(ConstrainPattern::getMessage)
                .forEach(messages::add);

        return messages;
    }
}
