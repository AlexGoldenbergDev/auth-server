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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Supplier;


@Service
@Slf4j
@Transactional
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
    @Cacheable(value = "allUsers")
    public List<User> getAll() {
        TreeSet<User> users = new TreeSet<>(Comparator.comparing(User::getUsername));
        users.addAll(userDao.findAll());
        return new ArrayList<>(users);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userById", key = "#id"),
            @CacheEvict(value = "servicePrints", key = "#id"),
            @CacheEvict(value = {"userByLogin", "userByEmail", "allUsers"}, allEntries = true)
    })
    public void deleteById(UUID id) {
        userDao.deleteById(id);
    }

    @Override
    @CachePut(value = "userById", key = "#id")
    public Optional<User> findById(UUID id) {
        return userDao.findById(id);
    }

    @Override
    public boolean isSignedUp(RequestForm requestForm) {
        String email = requestForm.getEmail();
        return findUserByEmail(email).isPresent();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userById", key = "#dto.id"),
            @CacheEvict(value = "servicePrints", key = "#dto.id"),
            @CacheEvict(value = {"userByLogin", "userByEmail", "allUsers"}, allEntries = true)
    })
    public void changeRole(ChangeRoleDto dto) {
        if (configurationService.getRoles().contains(dto.getRole())) {
            UUID id = dto.getId();

            findById(id).ifPresent(user -> {
                String role = dto.getRole();
                log.debug("Changing user {} role to {}", id, role);
                user.setRole(role);
                userDao.save(user);
            });
        }


    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "userById", key = "#id"),
            @CacheEvict(value = "servicePrints", key = "#id"),
            @CacheEvict(value = {"userByLogin", "userByEmail", "allUsers"}, allEntries = true)
    })
    public void changeEnabledStatus(UUID id, boolean status) {
        findById(id).ifPresent(user -> {
            user.setEnabled(status);
            userDao.save(user);
        });

    }


    @Override
    @Caching(evict = {
            @CacheEvict(value = "userByEmail", key = "#token.email"),
            @CacheEvict(value = {"userByLogin", "userById", "allUsers", "servicePrints"}, allEntries = true)
    })
    public Optional<User> resetPassword(PasswordResetToken token, PasswordResetForm form) {
        return userDao.findUserByEmail(token.getEmail()).map(user -> {
            user.setPassword(passwordEncoder.encode(form.getMatchingPassword()));
            return userDao.save(user);
        });


    }

    @Override
    @Cacheable(value = "userByLogin", key = "#login")
    public Optional<User> findByLogin(String login) {
        return userDao.findUserByUsername(login);
    }

    @Override
    @Cacheable(value = "userByEmail", key = "#email", unless = "#result == null")
    public Optional<User> findUserByEmail(@NotNull @NotEmpty String email) {
        return userDao.findUserByEmail(email);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "userById", key = "#userDto.uuid"),
                    @CacheEvict(value = "servicePrints", key = "#userDto.uuid"),
                    @CacheEvict(value = "userByLogin", key = "#userDto.login"),
                    @CacheEvict(value = "userByEmail", key = "#invitationToken.email"),
                    @CacheEvict(value = {"allUsers"}, allEntries = true)
            })
    public User create(UserDto userDto, InvitationToken invitationToken) {
        String role = configurationService.getDefaultRole();
        User user = new User(userDto.getUuid(), userDto.getLogin(), invitationToken.getEmail(), role,
                passwordEncoder.encode(userDto.getPassword()));
        return userDao.save(user);

    }


}
