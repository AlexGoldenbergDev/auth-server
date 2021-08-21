package com.goldenebrg.authserver.rest.beans;

import lombok.Data;

import java.util.UUID;

@Data
public class ChangeRoleDto {

    UUID id;
    String role;
}
