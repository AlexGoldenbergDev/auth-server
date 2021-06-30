package com.goldenebrg.authserver.rest.beans;

import lombok.Data;

@Data
public class PasswordResetForm {
    String password;
    String passwordConfirm;
}
