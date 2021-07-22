package com.goldenebrg.authserver.security.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface AppUserDetails extends UserDetails {

    Map<String, Object> getClaims();
}
