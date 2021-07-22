package com.goldenebrg.authserver.rest.beans;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class PasswordResetForm implements PasswordInputForm {

    @NotNull
    @NotEmpty
    String password;

    @NotNull
    @NotEmpty
    String matchingPassword;

}
