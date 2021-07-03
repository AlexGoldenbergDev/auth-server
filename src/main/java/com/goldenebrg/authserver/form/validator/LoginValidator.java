package com.goldenebrg.authserver.form.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LoginValidator implements ConstraintValidator<ValidLogin, String> {

    private static final int MIN = 3;
    private static final int MAX = 20;

    @Override
    public void initialize(ValidLogin constraintAnnotation) {}

    @Override
    public boolean isValid(String login, ConstraintValidatorContext context){
        return (validateLogin(login));
    }
    private boolean validateLogin(String login) {
        return
                login != null &&
                !login.isEmpty() &&
                login.length() >= MIN &&
                login.length() <= MAX;

    }
}
