package com.goldenebrg.authserver.rest.beans;

import lombok.Data;

import java.io.Serializable;

@Data
public class RequestForm implements Serializable {

    String email;
}
