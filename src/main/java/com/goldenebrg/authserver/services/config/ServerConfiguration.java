package com.goldenebrg.authserver.services.config;

import lombok.Data;

@Data
public class ServerConfiguration {

    String address;
    CorsConfiguration cors;
}
