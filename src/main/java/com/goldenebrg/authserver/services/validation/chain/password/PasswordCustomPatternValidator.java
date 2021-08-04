package com.goldenebrg.authserver.services.validation.chain.password;

import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.config.ConstrainPattern;
import com.goldenebrg.authserver.services.validation.chain.AbstractPasswordValidator;
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
public class PasswordCustomPatternValidator extends AbstractPasswordValidator implements CustomPatternValidator {

    ConstrainPattern constrainPattern;

    @Autowired
    public PasswordCustomPatternValidator(ServerConfigurationService serverConfigurationService) {
        super(serverConfigurationService);
    }

    @Override
    protected Optional<String> validate(@NotNull @NotEmpty String password0, @NotNull @NotEmpty String password1) {
        return validatePattern(password0);
    }

    @Override
    public ConstrainPattern getConstrainPattern() {
        return constrainPattern;
    }

    public void setConstrainPattern(ConstrainPattern constrainPattern) {
        this.constrainPattern = constrainPattern;
    }
}
