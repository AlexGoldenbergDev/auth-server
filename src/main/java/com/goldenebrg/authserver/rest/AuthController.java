package com.goldenebrg.authserver.rest;

import com.goldenebrg.authserver.index.ServiceName;
import com.goldenebrg.authserver.rest.beans.AuthRequest;
import com.goldenebrg.authserver.rest.beans.AuthValidationRequest;
import com.goldenebrg.authserver.rest.beans.AuthValidationResponse;
import com.goldenebrg.authserver.security.LoginAttemptService;
import com.goldenebrg.authserver.security.auth.service.AppUserDetails;
import com.goldenebrg.authserver.security.auth.service.JwtService;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static com.goldenebrg.authserver.security.auth.service.UserDetailsServiceImpl.getClientIP;

/**
 * @author Alex Goldenberg
 * REST Controller for Authentication from External origins using JWT token
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final ServerConfigurationService serverConfigurationService;
    private int loginAttemptTimeoutMinutes;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          @Qualifier(ServiceName.USER_DETAIL_SERVICE)
                                  UserDetailsService userDetailsService,
                          JwtService jwtService,
                          LoginAttemptService loginAttemptService,
                          ServerConfigurationService serverConfigurationService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.serverConfigurationService = serverConfigurationService;
    }


    @PostConstruct
    void initialize() {
        this.loginAttemptTimeoutMinutes = this.serverConfigurationService.getLoginAttemptTimeoutMinutes();
    }

    /**
     * Creates a new JWT token
     * authRequest - {@link AuthRequest} form with user credentials
     */
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> auth(@RequestBody final AuthRequest authRequest, HttpServletRequest request) {
        Optional<ResponseEntity<?>> responseEntity = Optional.empty();
        String clientIP = getClientIP(request);
        String username = authRequest.getUsername();


        if (loginAttemptService.isBlocked(clientIP)) {
            String message = String.format("Too many failed login attempts. Try again in %d minutes", loginAttemptTimeoutMinutes);
            responseEntity = Optional.of(AuthResponseEntityTool.authTokenResponse(authRequest, message, HttpStatus.LOCKED));
        } else {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, authRequest.getPassword());

            try {
                this.authenticationManager.authenticate(authToken);
            } catch (UsernameNotFoundException e) {
                responseEntity = Optional.of(AuthResponseEntityTool.authTokenResponse(authRequest, e, HttpStatus.UNAUTHORIZED));
                loginAttemptService.loginFailed(clientIP);
            } catch (AuthenticationException e) {
                responseEntity = Optional.of(AuthResponseEntityTool.authTokenResponse(authRequest, e, HttpStatus.FORBIDDEN));
                loginAttemptService.loginFailed(clientIP);
            }
        }


        return responseEntity.orElseGet(() -> {
            AppUserDetails userDetails = (AppUserDetails) userDetailsService.loadUserByUsername(username);
            String token = jwtService.generateToken(userDetails);
            loginAttemptService.loginSucceeded(clientIP);
            return AuthResponseEntityTool.authTokenResponse(authRequest, token);
        });
    }


    /**
     * Validates JWT token
     * authRequest - {@link AuthRequest} form with user credentials
     */
    @PostMapping(value = "/validation",  produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthValidationResponse> validation(
            @RequestHeader("Authorization") final String authorization,
            @RequestBody final AuthValidationRequest request) {
        String user;
        String jwt;

        HttpStatus status = null;

        if(authorization != null && authorization.startsWith("Bearer ")) {
            jwt = authorization.substring(7);
            try {
                user = jwtService.getUsernameFromToken(jwt);
                if (user != null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(user);
                    status = jwtService.validateToken(jwt, userDetails) ? HttpStatus.OK : HttpStatus.FORBIDDEN;
                }
                else status = HttpStatus.UNAUTHORIZED;

            } catch (ExpiredJwtException e) {
                status = HttpStatus.UNAUTHORIZED;
            }
        }

        return AuthResponseEntityTool.authValidationResponse(request, status);
    }

}
