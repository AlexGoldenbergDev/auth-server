package com.goldenebrg.authserver.services.validation.chain.password;

import com.goldenebrg.authserver.rest.beans.PasswordInputForm;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.validation.chain.AbstractPasswordValidator;
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
public class PasswordSizeValidator extends AbstractPasswordValidator implements SizeValidator<PasswordInputForm> {


    @Autowired
    public PasswordSizeValidator(ServerConfigurationService serverConfigurationService) {
        super(serverConfigurationService);
    }

    @Override
    protected Optional<String> validate(@NotNull @NotEmpty String password0, @NotNull @NotEmpty String password1) {
        return validateSize(password0);
    }

    @Override
    public String getMessage() {
        return String.format("Passwords size must be between %d and %d characters", getMinSize(), getMaxSize());
    }

    @Override
    public int getMinSize() {
        return configurationService.getPasswordMinSize();
    }

    @Override
    public int getMaxSize() {
        return configurationService.getPasswordMaxSize();
    }
}
