package com.goldenebrg.authserver.services.validation.chain;

import com.goldenebrg.authserver.rest.beans.UserDto;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.validation.chain.login.LoginValidator;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public abstract class AbstractLoginValidator
        extends AbstractUserValidator<UserDto>
        implements LoginValidator {

    public AbstractLoginValidator(
            ServerConfigurationService serverConfigurationService) {
        super(serverConfigurationService);

    }

    @Override
    public List<String> validate(List<String> messages, UserDto dto) {
        @NotNull @NotEmpty String login = dto.getLogin();
        validate(login).ifPresent(messages::add);
        return next == null ? messages : next.validate(messages, dto);
    }

    protected abstract Optional<String> validate(@NotNull @NotEmpty String login);
}
