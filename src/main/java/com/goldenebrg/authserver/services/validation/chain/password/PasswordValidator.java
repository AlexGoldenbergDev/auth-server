package com.goldenebrg.authserver.services.validation.chain.password;

import com.goldenebrg.authserver.rest.beans.PasswordInputForm;
import com.goldenebrg.authserver.services.validation.chain.UserValidator;

public interface PasswordValidator extends UserValidator<PasswordInputForm> {
}
