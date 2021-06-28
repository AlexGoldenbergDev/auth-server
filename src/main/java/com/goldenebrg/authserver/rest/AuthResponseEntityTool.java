package com.goldenebrg.authserver.rest;

import com.goldenebrg.authserver.rest.beans.AuthRequest;
import com.goldenebrg.authserver.rest.beans.AuthTokenResponse;
import com.goldenebrg.authserver.rest.beans.AuthValidationRequest;
import com.goldenebrg.authserver.rest.beans.AuthValidationResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AuthResponseEntityTool {

    static @NonNull ResponseEntity<AuthTokenResponse> authTokenResponse(@NonNull AuthRequest request,
                                                                        @NonNull String token) {
        return create(new AuthTokenResponse(request.getId(), token, "Authorized"));
    }


    static @NonNull ResponseEntity<AuthTokenResponse> authTokenResponse(@NonNull AuthRequest request,
                                                                          @NonNull Exception exception,
                                                                          @NonNull HttpStatus status) {
        return create(new AuthTokenResponse(request.getId(), null, exception.getMessage()), status);
    }


    static @NonNull ResponseEntity<AuthValidationResponse> authValidationResponse(@NonNull AuthValidationRequest request,
                                                                                  @NonNull HttpStatus status) {
        return create(new AuthValidationResponse(request.getId()), status);
    }


    private static  <T> @NonNull ResponseEntity<T> create(@NonNull T dto, @NonNull HttpStatus status) {
        return new ResponseEntity<>(dto, status);
    }


    private static  <T> @NonNull ResponseEntity<T> create(@NonNull T dto) {
        return ResponseEntity.ok(dto);
    }

}
