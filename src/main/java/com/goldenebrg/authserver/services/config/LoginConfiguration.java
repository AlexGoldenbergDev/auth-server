package com.goldenebrg.authserver.services.config;

import lombok.Data;

import java.util.List;

@Data
public class LoginConfiguration {

    int minSize;
    int maxSize;
    List<ConstrainPattern> patterns;
}
