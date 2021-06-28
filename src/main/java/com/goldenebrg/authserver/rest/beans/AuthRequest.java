package com.goldenebrg.authserver.rest.beans;

import lombok.Data;

@Data
public class AuthRequest {

   final String id;
   final String username;
   final String password;

}
