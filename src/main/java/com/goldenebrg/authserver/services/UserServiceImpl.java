package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.InvitationDao;
import com.goldenebrg.authserver.jpa.dao.PasswordResetDao;
import com.goldenebrg.authserver.jpa.dao.TokensDao;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.rest.beans.PasswordResetForm;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.rest.beans.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Supplier;


@Service
@Slf4j
public class UserServiceImpl implements UserService{

    private final ServerConfigurationService configurationService;
    private final PasswordEncoder passwordEncoder;
    private final InvitationDao invitationDao;
    private final PasswordResetDao passwordResetDao;
    private final UserDao userDao;


    @Autowired
    public UserServiceImpl(
            ServerConfigurationService configurationService,
            PasswordEncoder passwordEncoder,
            InvitationDao invitationDao,
            PasswordResetDao passwordResetDao,
            UserDao userDao) {
        this.configurationService = configurationService;
        this.passwordEncoder = passwordEncoder;
        this.invitationDao = invitationDao;
        this.passwordResetDao = passwordResetDao;
        this.userDao = userDao;
    }



    @Override
    public List<String> getAvailableRoles() {
        return configurationService.getRoles();
    }

    @Scheduled(cron = "0 0 0 * *")
    public void deleteOldRequests() {
        Date startTime = new Date();
        deleteOldRequests(startTime, configurationService::getSignUpTokenExpirationHours, invitationDao);
        deleteOldRequests(startTime, configurationService::getPasswordResetTokenExpirationHours, passwordResetDao);
    }

    void deleteOldRequests(Date startTime, Supplier<Integer> supplier, TokensDao dao) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.HOUR, -1 * supplier.get());
        Date time = calendar.getTime();
        dao.deleteAllByCreationDateBefore(time);
    }


    @Override
    public List<User> getAll() {
        TreeSet<User> users = new TreeSet<>(Comparator.comparing(User::getUsername));
        users.addAll(userDao.findAll());
        return new ArrayList<>(users);
    }

    @Override
    public void deleteById(UUID id) {
        userDao.deleteById(id);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userDao.findById(id);
    }

    @Override
    public boolean isSignedUp(RequestForm requestForm) {
        String email = requestForm.getEmail();
        return userDao.findUserByEmail(email).isPresent();
    }

    @Override
    public void changeRole(ChangeRoleDto dto) {
        if (configurationService.getRoles().contains(dto.getRole())) {
            String id = dto.getId();
            findById(UUID.fromString(id)).ifPresent(user -> {
                configurationService.getRoles();
                user.setRole(dto.getRole());
                userDao.save(user);
            });
        }


    }

    @Override
    public void changeEnabledStatus(UUID id, boolean status) {
        findById(id).ifPresent(user -> {
            user.setEnabled(status);
            userDao.save(user);
        });

    }


    @Override
    public Optional<User> resetPassword(PasswordResetToken token, PasswordResetForm form) {
        return userDao.findUserByEmail(token.getEmail()).map(user -> {
            user.setPassword(passwordEncoder.encode(form.getMatchingPassword()));
            return userDao.save(user);
        });


    }

    @Override
    public Optional<User> findByLogin(String login) {
        return userDao.findUserByUsername(login);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userDao.findUserByEmail(email);
    }

    @Override
    public User create(UserDto userDto, InvitationToken invitationToken) {
        String role = configurationService.getDefaultRole();
        User user = new User(userDto.getUuid(), userDto.getLogin(), invitationToken.getEmail(), role,
                passwordEncoder.encode(userDto.getPassword()));
        return userDao.save(user);

    }


}
