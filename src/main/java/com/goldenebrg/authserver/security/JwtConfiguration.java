package com.goldenebrg.authserver.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:jwt.properties")
public class JwtConfiguration {
}
