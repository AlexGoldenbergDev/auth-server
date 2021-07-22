package com.goldenebrg.authserver.rest.beans;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public interface PasswordInputForm {

    @NotNull @NotEmpty String getPassword();

    @NotNull @NotEmpty String getMatchingPassword();
}
