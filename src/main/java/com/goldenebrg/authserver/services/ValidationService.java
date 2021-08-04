package com.goldenebrg.authserver.services;

import javax.validation.constraints.NotNull;
import java.util.List;

@FunctionalInterface
public interface ValidationService<T> {

    List<String> validate(@NotNull T dto);
}
