package com.goldenebrg.authserver.rest.beans;

import com.goldenebrg.authserver.form.validator.ValidLogin;
import com.goldenebrg.authserver.form.validator.ValidPassword;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDto extends LoginDto{

    private UUID uuid;

    @ValidLogin
    private String login;

    @ValidPassword
    private String password;

    @ValidPassword
    private String matchingPassword;


}
