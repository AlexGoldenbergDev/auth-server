package com.goldenebrg.authserver.services.config;

import lombok.Data;

import java.util.List;

@Data
public class PasswordConfiguration {

    int minSize;
    int maxSize;
    List<ConstrainPattern> patterns;
}
