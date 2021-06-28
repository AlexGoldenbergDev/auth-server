package com.goldenebrg.authserver.rest.beans;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDto extends LoginDto{

    private UUID uuid;
    private String matchingPassword;


}
