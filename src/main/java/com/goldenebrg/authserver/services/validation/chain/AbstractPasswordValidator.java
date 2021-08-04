package com.goldenebrg.authserver.services.validation.chain;

import com.goldenebrg.authserver.rest.beans.PasswordInputForm;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.validation.chain.password.PasswordValidator;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public abstract class AbstractPasswordValidator
        extends AbstractUserValidator<PasswordInputForm>
        implements PasswordValidator {


    public AbstractPasswordValidator(ServerConfigurationService serverConfigurationService) {
        super(serverConfigurationService);
    }

    @Override
    public List<String> validate(List<String> messages, PasswordInputForm dto) {
        @NotNull @NotEmpty String password0 = dto.getPassword();
        @NotNull @NotEmpty String password1 = dto.getMatchingPassword();

        validate(password0, password1).ifPresent(messages::add);
        return next == null ? messages : next.validate(messages, dto);
    }

    protected abstract Optional<String> validate(@NotNull @NotEmpty String password0,
                                                 @NotNull @NotEmpty String password1);
}
