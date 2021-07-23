package com.goldenebrg.authserver.security.auth.service;

import com.goldenebrg.authserver.index.ServiceName;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.security.LoginAttemptService;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;


@Service(ServiceName.USER_DETAIL_SERVICE)
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDao userRepository;
    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;
    private final ServerConfigurationService serverConfigurationService;
    private int loginAttemptTimeoutMinutes;


    @Autowired
    UserDetailsServiceImpl(ServerConfigurationService serverConfigurationService, UserDao userDao, LoginAttemptService loginAttemptService, HttpServletRequest request) {
        this.serverConfigurationService = serverConfigurationService;
        this.userRepository = userDao;
        this.loginAttemptService = loginAttemptService;
        this.request = request;
    }

    public static String getClientIP(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(xh -> xh.split(",")[0]).orElse(request.getRemoteAddr());
    }

    @PostConstruct
    void initialize() {
        this.loginAttemptTimeoutMinutes = serverConfigurationService.getLoginAttemptTimeoutMinutes();
    }

    @Override
    public AppUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        String ip = getClientIP(request);
        if (loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException(String.format("Too many login attempts. Try in %d minutes", loginAttemptTimeoutMinutes));
        }

        User user = userRepository.findUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("Could not find user");
        }

        return new UserDetailsImpl(user);
    }

}
