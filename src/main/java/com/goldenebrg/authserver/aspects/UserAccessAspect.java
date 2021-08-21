package com.goldenebrg.authserver.aspects;

import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.mvc.exceptions.UnknownUserLoginAttempt;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class UserAccessAspect {

    private final UserDao userDao;

    @Autowired
    public UserAccessAspect(UserDao userDao) {
        this.userDao = userDao;
    }

    @Around("@annotation(UserAccess)")
    Object userAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        log.trace("Checking user access...");
        Object[] args = joinPoint.getArgs();

        int length = args.length;

        UserDetails userDetails;
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            User user = userDao.findUserByUsername(username)
                    .orElseThrow(UnknownUserLoginAttempt::new);

            log.debug("User '{}' is authorized", username);

            if (args[length - 1] == null)
                args[length - 1] = user;

            return joinPoint.proceed(args);

        } catch (ClassCastException | NullPointerException exception) {
            throw new UnknownUserLoginAttempt();
        }

    }
}
