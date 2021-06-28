package com.goldenebrg.authserver.rest.beans;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDto extends LoginDto{

    private UUID uuid;
    private String email;
    private String matchingPassword;


}
