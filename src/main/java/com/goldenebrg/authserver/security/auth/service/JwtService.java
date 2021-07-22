package com.goldenebrg.authserver.security.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Management Service
 */
@Service
public class JwtService {


    @Value("${jwt.secret}")
    private String secret;

    /**
     * Return user name associated with JWT token
     * @param token - {@link String} JWT token
     * @return {@link String} user name
     * @throws ExpiredJwtException - if JWT token already expired
     */
    public String getUsernameFromToken(String token) throws ExpiredJwtException{
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Return expiration date of JWT token
     * @param token - {@link String} JWT token
     * @return {@link Date}expiration date
     * @throws ExpiredJwtException - if JWT token already expired
     */
    public Date getExpirationDateFromToken(String token) throws ExpiredJwtException{
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Return  JWT token claim
     * @param token - {@link String} JWT token
     * @param claimsResolver - {@link Function} specific claim provider
     * @return specific claim
     * @throws ExpiredJwtException - if JWT token already expired
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) throws ExpiredJwtException {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Return all JWT token claims
     * @param token - {@link String} JWT token
     * @return all {@link Claims}
     * @throws ExpiredJwtException - if JWT token already expired
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * Validates if JWT token expired
     * @param token - {@link String} JWT token
     * @return all {@link Claims}
     * @throws ExpiredJwtException - if JWT token already expired
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(AppUserDetails userDetails) {
        Map<String, Object> claims = userDetails.getClaims();
        return doGenerateToken(claims, userDetails.getUsername());
    }


    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(createExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    private Date createExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 3);
        return calendar.getTime();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
