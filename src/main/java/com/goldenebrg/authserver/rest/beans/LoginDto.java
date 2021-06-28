package com.goldenebrg.authserver.rest.beans;

import lombok.Data;

@Data
public class LoginDto {

    private String login;

    private String password;
}
