package com.goldenebrg.authserver.services.validation.chain;

import com.goldenebrg.authserver.services.ServerConfigurationService;

abstract class AbstractUserValidator<T> implements UserValidator<T> {

    protected final ServerConfigurationService configurationService;
    protected UserValidator<T> next;

    public AbstractUserValidator(
            ServerConfigurationService serverConfigurationService) {
        this.configurationService = serverConfigurationService;
    }

    @Override
    public void setNext(UserValidator<T> next) {
        this.next = next;
    }
}
