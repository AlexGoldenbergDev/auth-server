package com.goldenebrg.authserver.services.validation.chain.password;

import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.validation.chain.AbstractPasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PasswordMatchValidator extends AbstractPasswordValidator {


    @Autowired
    public PasswordMatchValidator(ServerConfigurationService serverConfigurationService) {
        super(serverConfigurationService);
    }

    @Override
    protected Optional<String> validate(@NotNull @NotEmpty String password0, @NotNull @NotEmpty String password1) {
        Optional<String> message;

        if (password0.equals(password1))
            message = Optional.empty();
        else
            message = Optional.of("Passwords doesn't match each other");

        return message;
    }
}
