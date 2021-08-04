package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.rest.beans.PasswordInputForm;
import com.goldenebrg.authserver.services.config.ConstrainPattern;
import com.goldenebrg.authserver.services.validation.chain.password.PasswordCustomPatternValidator;
import com.goldenebrg.authserver.services.validation.chain.password.PasswordMatchValidator;
import com.goldenebrg.authserver.services.validation.chain.password.PasswordSizeValidator;
import com.goldenebrg.authserver.services.validation.chain.password.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordValidationServiceImpl implements PasswordValidationService {

    private final ApplicationContext applicationContext;
    private final ServerConfigurationService configurationService;

    private PasswordValidator validator;
    private int counter;

    @Autowired
    public PasswordValidationServiceImpl(ApplicationContext applicationContext,
                                         ServerConfigurationService configurationService) {
        this.applicationContext = applicationContext;
        this.configurationService = configurationService;
    }


    @PostConstruct
    void initialize() {
        PasswordValidator next = applicationContext.getBean(PasswordMatchValidator.class);
        PasswordValidator validator = applicationContext.getBean(PasswordSizeValidator.class);

        this.validator = next;

        next.setNext(validator);
        next = validator;

        this.counter = 2;

        for (ConstrainPattern pattern : configurationService.getPasswordPatterns()) {
            PasswordCustomPatternValidator bean = applicationContext.getBean(PasswordCustomPatternValidator.class);
            bean.setConstrainPattern(pattern);
            next.setNext(bean);
            next = bean;
            this.counter++;
        }
    }


    @Override
    public List<String> validate(@NotNull PasswordInputForm dto) {
        List<String> messages = new ArrayList<>(this.counter);
        return validator.validate(messages, dto);
    }
}
