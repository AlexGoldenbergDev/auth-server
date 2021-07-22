package com.goldenebrg.authserver.services.config;

import lombok.Data;

import java.util.List;

@Data
public class CorsConfiguration {

    List<String> methods;
    List<String> headers;
    List<String> origins;
}
