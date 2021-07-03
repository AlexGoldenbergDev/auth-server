package com.goldenebrg.authserver.rest.beans;

import com.goldenebrg.authserver.form.validator.ValidEmail;
import lombok.Data;

import java.io.Serializable;

@Data
public class RequestForm implements Serializable {

    @ValidEmail
    String email;
}
