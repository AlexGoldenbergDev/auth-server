package com.goldenebrg.authserver.services.validation.chain;

import java.util.List;

public interface UserValidator<T> {

    List<String> validate(List<String> messages, T dto);

    void setNext(UserValidator<T> next);
}
