package com.goldenebrg.authserver.rest.beans;

import com.goldenebrg.authserver.form.validator.ValidPassword;
import lombok.Data;

@Data
public class PasswordResetForm {
    @ValidPassword
    String password;

    @ValidPassword
    String passwordConfirm;
}
