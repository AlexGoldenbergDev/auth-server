package com.goldenebrg.authserver.security.auth.service;

import com.goldenebrg.authserver.index.ServiceName;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service(ServiceName.USER_DETAIL_SERVICE)
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserDao userRepository;

    @Autowired
    UserDetailsServiceImpl(UserDao userDao) {
        this.userRepository = userDao;
    }


    @Override
    public AppUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("Could not find user");
        }

        return new UserDetailsImpl(user);
    }

}
