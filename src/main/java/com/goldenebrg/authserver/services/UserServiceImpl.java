package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.InvitationDao;
import com.goldenebrg.authserver.jpa.dao.PasswordResetDao;
import com.goldenebrg.authserver.jpa.dao.TokensDao;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.mail.MailService;
import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.rest.beans.UserDto;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Supplier;

@Service
public class UserServiceImpl implements UserService{

    private final ServerConfigurationService configurationService;
    private final PasswordEncoder passwordEncoder;
    private final InvitationDao invitationDao;
    private final PasswordResetDao passwordResetDao;
    private final UserDao userDao;
    private final MailService mailService;
    private List<String> userRoles;


    @Autowired
    public UserServiceImpl(MailService mailService,
                           ServerConfigurationService configurationService,
                           PasswordEncoder passwordEncoder,
                           InvitationDao invitationDao,
                           PasswordResetDao passwordResetDao,
                           UserDao userDao) {
        this.mailService = mailService;
        this.configurationService = configurationService;
        this.passwordEncoder = passwordEncoder;
        this.invitationDao = invitationDao;
        this.passwordResetDao = passwordResetDao;
        this.userDao = userDao;
    }

    @PostConstruct
    void initialize() {
        this.userRoles = configurationService.getRoles();
    }

    @Override
    public List<String> getAvailableRoles() {
        return userRoles;
    }

    @Override
    public void createInvitation(@NotNull RequestForm requestForm) {
        UUID uuid = createUniqueUUID();
        InvitationToken invitationToken = new InvitationToken(uuid, new Date(), requestForm.getEmail());
        mailService.sendSignUpRequest(invitationDao.save(invitationToken));
    }

    @Override
    public void createPasswordReset(@NotNull RequestForm requestForm) {
        String email = requestForm.getEmail();
        Optional.ofNullable(userDao.findUserByEmail(email)).ifPresent(user -> {
            UUID uuid = createUniqueUUID();
            PasswordResetToken passwordResetToken = new PasswordResetToken(uuid, new Date(), requestForm.getEmail());
            mailService.sendPasswordResetEmail(passwordResetDao.save(passwordResetToken));
        });

    }

    /**
     * Creating unique {@link UUID}
     * @return unique {@link UUID}
     */
    private UUID createUniqueUUID() {
        UUID uuid = null;
        boolean isExists = true;
        while (isExists) {
            uuid = UUID.randomUUID();
            isExists = isRequestUUIDExists(uuid);
        }
        return uuid;
    }


    @Override
    public boolean isRequestUUIDExists(UUID uuid) {
        return invitationDao.existsById(uuid);
    }


    @Override
    public List<InvitationToken> getInvitations() {
        return invitationDao.findAll();
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
    public void deleteRequestById(UUID uuid) {
        invitationDao.deleteById(uuid);
    }


    @Override
    public User registerNewUserAccount(@NonNull UserDto userDto, UUID requestIid) {
        InvitationToken invitationToken = invitationDao.getOne(requestIid);
        String role = configurationService.getDefaultRole();
        User user = new User(userDto.getUuid(), userDto.getLogin(), invitationToken.getEmail(), role,
                passwordEncoder.encode(userDto.getPassword()));
        userDao.save(user);
        deleteRequestById(requestIid);
        return user;

    }

    @Override
    public List<User> getUsers() {
        return userDao.findAll();
    }

    @Override
    public void deleteUserById(UUID id) {
        userDao.deleteById(id);
    }

    @Override
    public User getUserById(UUID id) {
        return userDao.getOne(id);
    }

    @Override
    public void changeRole(ChangeRoleDto dto) {
        String id = dto.getId();
        User user = getUserById(UUID.fromString(id));
        user.setRole(dto.getRole());
        userDao.save(user);
    }

    @Override
    public void toggleEnabledStatus(String id, boolean status) {
        User user = getUserById(UUID.fromString(id));
        user.setEnabled(status);
        userDao.save(user);
    }
}
