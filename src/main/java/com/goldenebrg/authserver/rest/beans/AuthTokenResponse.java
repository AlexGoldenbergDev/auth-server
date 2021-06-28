package com.goldenebrg.authserver.rest.beans;

import lombok.Data;
import lombok.NonNull;


@Data
public class AuthTokenResponse {

    @NonNull
    final String id;
    final String token;
    @NonNull
    final String message;

}
