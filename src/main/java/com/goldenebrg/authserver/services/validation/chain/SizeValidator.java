package com.goldenebrg.authserver.services.validation.chain;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface SizeValidator<T> extends UserValidator<T> {

    default Optional<String> validateSize(@NotNull @NotEmpty String string) {
        Optional<String> message;

        int maxSize = getMaxSize();
        int minSize = getMinSize();
        if (string.length() > maxSize || string.length() < minSize)
            message = Optional.of(getMessage());
        else
            message = Optional.empty();

        return message;
    }


    String getMessage();

    int getMinSize();

    int getMaxSize();
}
