package com.goldenebrg.authserver.services.validation.chain.login;

import com.goldenebrg.authserver.rest.beans.UserDto;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.validation.chain.AbstractLoginValidator;
import com.goldenebrg.authserver.services.validation.chain.SizeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoginSizeValidator extends AbstractLoginValidator implements SizeValidator<UserDto> {

    @Autowired
    public LoginSizeValidator(ServerConfigurationService serverConfigurationService) {
        super(serverConfigurationService);
    }


    @Override
    public Optional<String> validate(@NotNull @NotEmpty String login) {
        return validateSize(login);
    }

    @Override
    public String getMessage() {
        return String
                .format("Login size must be between %d and %d characters",
                        getMinSize(), getMaxSize());
    }

    @Override
    public int getMinSize() {
        return configurationService.getLoginMinSize();
    }

    @Override
    public int getMaxSize() {
        return configurationService.getLoginMaxSize();
    }
}
