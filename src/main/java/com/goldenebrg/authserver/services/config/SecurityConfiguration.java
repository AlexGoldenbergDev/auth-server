package com.goldenebrg.authserver.services.config;

import lombok.Data;

@Data
public class SecurityConfiguration {

    LoginConfiguration login;
    PasswordConfiguration password;
}
