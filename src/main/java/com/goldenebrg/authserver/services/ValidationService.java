package com.goldenebrg.authserver.services;

import java.util.List;

@FunctionalInterface
public interface ValidationService<T> {

    List<String> validate(T dto);
}
