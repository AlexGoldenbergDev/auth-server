package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.rest.beans.UserDto;
import com.goldenebrg.authserver.services.config.ConstrainPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class LoginValidationServiceImpl implements LoginValidationService {


    private final ServerConfigurationService configurationService;
    private final UserService userService;

    @Autowired
    public LoginValidationServiceImpl(ServerConfigurationService configurationService, UserService userService) {
        this.configurationService = configurationService;
        this.userService = userService;
    }

    @Override
    public List<String> validate(UserDto dto) {
        String login = dto.getLogin();

        List<String> messages = new LinkedList<>();

        int loginMaxSize = configurationService.getLoginMaxSize();
        int loginMinSize = configurationService.getLoginMinSize();
        if (login.length() > loginMaxSize || login.length() < loginMinSize)
            messages.add(String.format("Login size must be between %d and %d characters", loginMinSize, loginMaxSize));

        configurationService.getLoginPatterns().stream().filter(ptn -> !Pattern.compile(ptn.getPattern()).matcher(login).find())
                .map(ConstrainPattern::getMessage).forEach(messages::add);

        boolean isUserExists = Optional.ofNullable(userService.findByLogin(login)).isPresent();
        if (isUserExists) messages.add("User with this login already exists. Please, check for another option");

        return messages;
    }
}
