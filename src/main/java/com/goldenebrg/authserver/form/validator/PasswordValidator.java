package com.goldenebrg.authserver.form.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN = 6;
    private static final int MAX = 25;

    private static final String NUMBERS_PTN = "[0-9]";
    private static final String CAPITAL_PTN = "[A-Z]";
    private static final String REGULAR_PTN = "[a-z]";
    private static final String SPECIAL_PTN = "\\!|\\@|\\#|\\$|\\%|\\&|\\*|\\(" +
            "|\\)|\\'|\\+|\\,|\\-|\\.|\\/|\\:|\\;|\\<|\\=|\\>|\\?|\\[|\\]|\\^|\\_" +
            "|\\`|\\{|\\||\\}";

    @Override
    public void initialize(ValidPassword constraintAnnotation) {}

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context){
        return (validatePassword(password));
    }

    private boolean validatePassword(String password) {
        return
                password != null &&
                !password.isEmpty() &&
                password.length() >= MIN &&
                password.length() <= MAX &&
                password.matches(NUMBERS_PTN) &&
                password.matches(CAPITAL_PTN) &&
                password.matches(REGULAR_PTN) &&
                password.matches(SPECIAL_PTN)
                ;
    }
}
