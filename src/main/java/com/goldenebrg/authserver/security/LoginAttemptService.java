package com.goldenebrg.authserver.security;

import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginAttemptService {

    private final int loginMaxAttempts;

    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService(ServerConfigurationService serverConfigurationService) {
        super();
        this.loginMaxAttempts = serverConfigurationService.getLoginMaxAttempts();
        this.attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(serverConfigurationService.getLoginAttemptTimeoutMinutes(), TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
                    @ParametersAreNonnullByDefault
                    public Integer load(String key) {
                        return 0;
                    }
                });

    }


    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
        log.trace("Login succeeded for {}", key);
    }


    public void loginFailed(String key) {
        int attempts;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        log.trace("Login failed for {}, {} attempts left", key, loginMaxAttempts - attempts);
        attemptsCache.put(key, attempts);
    }


    public boolean isBlocked(String key) {
        boolean isBlocked;
        try {
            isBlocked = attemptsCache.get(key) >= loginMaxAttempts;
        } catch (ExecutionException e) {
            isBlocked = false;
        }
        log.trace("Access block status for {}: {}", key, isBlocked);
        return isBlocked;
    }


    public void forceLoginAttemptBlock(String key) {
        attemptsCache.put(key, loginMaxAttempts);
    }
}
