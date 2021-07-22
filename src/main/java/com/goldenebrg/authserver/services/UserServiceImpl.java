package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.InvitationDao;
import com.goldenebrg.authserver.jpa.dao.PasswordResetDao;
import com.goldenebrg.authserver.jpa.dao.TokensDao;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.mail.MailService;
import com.goldenebrg.authserver.rest.beans.*;
import com.goldenebrg.authserver.services.config.ConstrainPattern;
import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    private final ServerConfigurationService configurationService;
    private final PasswordEncoder passwordEncoder;
    private final InvitationDao invitationDao;
    private final PasswordResetDao passwordResetDao;
    private final UserDao userDao;
    private final MailService mailService;

    private List<String> userRoles;

    private List<ConstrainPattern> passwordPatterns;
    private List<ConstrainPattern> loginPatterns;

    private int passwordMinSize;
    private int passwordMaxSize;

    private int loginMinSize;
    private int loginMaxSize;


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

        this.passwordPatterns = configurationService.getPasswordPatterns();
        this.loginPatterns = configurationService.getLoginPatterns();

        this.passwordMinSize = configurationService.getPasswordMinSize();
        this.passwordMaxSize = configurationService.getPasswordMaxSize();

        this.loginMinSize = configurationService.getLoginMinSize();
        this.loginMaxSize = configurationService.getLoginMaxSize();

    }

    @Override
    public List<String> getAvailableRoles() {
        return userRoles;
    }

    @Override
    public List<String> getPasswordValidationErrors(@NotNull PasswordInputForm passwordInputForm) {
        String password = passwordInputForm.getPassword();
        String matchingPassword = passwordInputForm.getMatchingPassword();

        List<String> messages = new LinkedList<>();

        if (!password.equals(matchingPassword))
            messages.add("Passwords doesn't match each other");

        if (password.length() > passwordMaxSize || password.length() < passwordMinSize)
            messages.add(String.format("Passwords size must be between %d and %d characters", passwordMinSize, passwordMaxSize));

        passwordPatterns.stream().filter(ptn -> !Pattern.compile(ptn.getPattern()).matcher(password).find())
                .map(ConstrainPattern::getMessage)
                .forEach(messages::add);

        return messages;

    }

    @Override
    public List<String> getLoginValidationErrors(@NotNull UserDto userDto) {
        String login = userDto.getLogin();

        List<String> messages = new LinkedList<>();

        if (login.length() > loginMaxSize || login.length() < loginMinSize)
            messages.add(String.format("Login size must be between %d and %d characters", loginMinSize, loginMaxSize));

        loginPatterns.stream().filter(ptn -> !Pattern.compile(ptn.getPattern()).matcher(login).find())
                .map(ConstrainPattern::getMessage).forEach(messages::add);

        boolean isUserExists = Optional.ofNullable(userDao.findUserByUsername(login)).isPresent();
        if (isUserExists) messages.add("User with this login already exists. Please, check for another option");

        return messages;
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

    @Override
    public Collection<InvitationToken> getSortedInvitations() {
        Collection<InvitationToken> tokens = new TreeSet<>(Comparator.comparing(InvitationToken::getCreationDate));
        tokens.addAll(getInvitations());
        return tokens;

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
        try {
            InvitationToken invitationToken = invitationDao.getOne(requestIid);
            String role = configurationService.getDefaultRole();
            User user = new User(userDto.getUuid(), userDto.getLogin(), invitationToken.getEmail(), role,
                    passwordEncoder.encode(userDto.getPassword()));
            userDao.save(user);
            deleteRequestById(requestIid);
            return user;
        } catch (EntityNotFoundException entityNotFoundException) {
            throw new IllegalStateException("Unable to find invitation associated with new user", entityNotFoundException);
        }
    }


    @Override
    public List<User> getUsers() {
        return userDao.findAll();
    }

    @Override
    public Collection<User> getSortedUsers() {
        TreeSet<User> users = new TreeSet<>(Comparator.comparing(User::getUsername));
        users.addAll(getUsers());
        return users;
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
    public boolean isEmailSignedUp(RequestForm requestForm) {
        String email = requestForm.getEmail();
        return Optional.ofNullable(userDao.findUserByEmail(email)).isPresent();
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

    @Override
    public PasswordResetToken getPasswordToken(UUID id) {
        return passwordResetDao.getOne(id);
    }

    @Override
    public User resetPassword(UUID id, PasswordResetForm form) {
        PasswordResetToken token = getPasswordToken(id);
        @lombok.NonNull String email = token.getEmail();
        return Optional.ofNullable(userDao.findUserByEmail(email))
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(form.getMatchingPassword()));
                    userDao.save(user);
                    passwordResetDao.delete(token);
                    return user;
                }).orElse(null);
    }
}
