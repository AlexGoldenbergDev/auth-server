package com.goldenebrg.authserver.services.validation.chain.login;

import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.config.ConstrainPattern;
import com.goldenebrg.authserver.services.validation.chain.AbstractLoginValidator;
import com.goldenebrg.authserver.services.validation.chain.CustomPatternValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoginCustomPatternValidator extends AbstractLoginValidator implements CustomPatternValidator {

    private ConstrainPattern constrainPattern;

    @Autowired
    public LoginCustomPatternValidator(ServerConfigurationService serverConfigurationService) {
        super(serverConfigurationService);
    }

    @Override
    protected Optional<String> validate(@NotNull @NotEmpty String login) {
        return validatePattern(login);
    }

    @Override
    public ConstrainPattern getConstrainPattern() {
        return constrainPattern;
    }

    public void setConstrainPattern(ConstrainPattern constrainPattern) {
        this.constrainPattern = constrainPattern;
    }
}
