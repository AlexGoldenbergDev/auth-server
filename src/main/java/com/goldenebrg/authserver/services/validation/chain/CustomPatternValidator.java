package com.goldenebrg.authserver.services.validation.chain;

import com.goldenebrg.authserver.services.config.ConstrainPattern;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.regex.Pattern;

public interface CustomPatternValidator {

    default Optional<String> validatePattern(@NotNull @NotEmpty String string) {
        Optional<String> message;

        ConstrainPattern constrainPattern = getConstrainPattern();
        boolean isMatches = Pattern.compile(constrainPattern.getPattern()).matcher(string).find();

        if (isMatches)
            message = Optional.empty();
        else
            message = Optional.of(constrainPattern.getMessage());

        return message;
    }

    ConstrainPattern getConstrainPattern();
}
