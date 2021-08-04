package com.goldenebrg.authserver.services.validation.chain.login;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.UserService;
import com.goldenebrg.authserver.services.validation.chain.AbstractLoginValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoginExistenceValidator extends AbstractLoginValidator {

    private final UserService userService;

    @Autowired
    public LoginExistenceValidator(ServerConfigurationService serverConfigurationService,
                                   UserService userService) {
        super(serverConfigurationService);
        this.userService = userService;
    }

    @Override
    protected Optional<String> validate(String login) {
        Optional<String> message;
        Optional<User> user = userService.findByLogin(login);

        if (user.isPresent())
            message = Optional.of(("User with this login already exists. Please, check for another option"));
        else
            message = Optional.empty();

        return message;

    }
}
