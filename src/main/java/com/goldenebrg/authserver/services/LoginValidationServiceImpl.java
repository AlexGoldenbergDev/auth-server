package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.rest.beans.UserDto;
import com.goldenebrg.authserver.services.config.ConstrainPattern;
import com.goldenebrg.authserver.services.validation.chain.login.LoginCustomPatternValidator;
import com.goldenebrg.authserver.services.validation.chain.login.LoginExistenceValidator;
import com.goldenebrg.authserver.services.validation.chain.login.LoginSizeValidator;
import com.goldenebrg.authserver.services.validation.chain.login.LoginValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoginValidationServiceImpl implements LoginValidationService {

    private final ApplicationContext applicationContext;
    private final ServerConfigurationService configurationService;

    private LoginValidator validator;
    private int counter;

    @Autowired
    public LoginValidationServiceImpl(ApplicationContext applicationContext,
                                      ServerConfigurationService configurationService) {
        this.applicationContext = applicationContext;
        this.configurationService = configurationService;
    }


    @PostConstruct
    void initialize() {
        LoginValidator next = applicationContext.getBean(LoginExistenceValidator.class);
        LoginValidator validator = applicationContext.getBean(LoginSizeValidator.class);

        this.validator = next;

        next.setNext(validator);
        next = validator;

        this.counter = 2;

        for (ConstrainPattern pattern : configurationService.getLoginPatterns()) {
            LoginCustomPatternValidator bean = applicationContext.getBean(LoginCustomPatternValidator.class);
            bean.setConstrainPattern(pattern);
            next.setNext(bean);
            next = bean;
            this.counter++;
        }
    }


    @Override
    public List<String> validate(@NotNull UserDto dto) {
        List<String> messages = new ArrayList<>(this.counter);
        return validator.validate(messages, dto);
    }
}
