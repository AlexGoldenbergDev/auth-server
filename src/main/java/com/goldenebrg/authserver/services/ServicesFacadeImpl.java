package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserServices;
import com.goldenebrg.authserver.rest.beans.*;
import com.goldenebrg.authserver.security.LoginAttemptService;
import com.goldenebrg.authserver.security.auth.service.UserDetailsServiceImpl;
import com.goldenebrg.authserver.services.config.ServiceInputField;
import com.goldenebrg.authserver.services.config.ServiceSelectionListField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@Slf4j
public class ServicesFacadeImpl implements FacadeService {

    private final InvitationService invitationService;
    private final PasswordResetService passwordResetService;
    private final UserService userService;
    private final LoginValidationService loginValidationService;
    private final PasswordValidationService passwordValidationService;
    private final UserServicesManagementService userServicesManagementService;
    private final ServerConfigurationService configurationService;
    private final TranslateService translateService;
    private final LoginAttemptService loginAttemptService;

    @Autowired
    public ServicesFacadeImpl(InvitationService invitationService, PasswordResetService passwordResetService, UserService userService, LoginValidationService loginValidationService, PasswordValidationService passwordValidationService, UserServicesManagementService userServicesManagementService, ServerConfigurationService configurationService, TranslateService translateService, LoginAttemptService loginAttemptService) {
        this.invitationService = invitationService;
        this.passwordResetService = passwordResetService;
        this.userService = userService;
        this.loginValidationService = loginValidationService;
        this.passwordValidationService = passwordValidationService;
        this.userServicesManagementService = userServicesManagementService;
        this.configurationService = configurationService;
        this.translateService = translateService;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public boolean isInvitationExists(UUID uuid) {
        return invitationService.isExists(uuid);
    }

    @Override
    public List<InvitationToken> getAllInvitations() {
        return invitationService.getAll();
    }

    @Override
    public void createInvitation(RequestForm requestForm) {
        boolean isEmailSignedUp = userService.findUserByEmail(requestForm.getEmail()).isPresent();
        if (!isEmailSignedUp) invitationService.create(requestForm);
    }

    @Override
    public void deleteInvitation(UUID id) {
        invitationService.delete(id);
    }

    @Override
    public boolean isSignedUp(RequestForm requestForm) {
        return userService.isSignedUp(requestForm);
    }

    @Override
    public List<String> validatePassword(PasswordInputForm form) {
        return passwordValidationService.validate(form);
    }

    @Override
    public Optional<PasswordResetToken> resetPassword(RequestForm resetForm) {
        String email = resetForm.getEmail();
        return userService.findUserByEmail(email).map(passwordResetService::create);
    }

    @Override
    public Optional<PasswordResetToken> findPasswordToken(UUID id) {
        return passwordResetService.get(id);
    }

    @Override
    public Optional<User> resetPassword(UUID id, PasswordResetForm form) {
        log.debug("Committing password reset for token {}", id);
        Optional<PasswordResetToken> resetToken = passwordResetService.get(id);
        Optional<User> user = resetToken.flatMap(token -> userService.resetPassword(token, form));
        resetToken.ifPresent(passwordResetService::delete);
        return user;
    }

    @Override
    public Optional<User> signUp(UserDto userDto, UUID requestIid) {
        log.debug("Signing up user {} with token {}", userDto.getLogin(), requestIid);
        Optional<InvitationToken> invitationToken = invitationService.find(requestIid);
        Optional<User> user = invitationToken.map(token -> userService.create(userDto, token));
        invitationToken.ifPresent(invitationService::delete);
        return user;
    }

    @Override
    public List<String> validateLogin(UserDto userDto) {
        return loginValidationService.validate(userDto);
    }

    @Override
    public void createService(UUID id, ServiceForm dto) {
        userService.findById(id).ifPresent(u -> userServicesManagementService.create(u, dto));
    }

    @Override
    public void deleteService(UUID id) {
        userServicesManagementService.delete(id);
    }

    @Override
    public void deleteService(String username, String service) {
        userService.findByLogin(username).map(User::getUserServices)
                .flatMap(services -> Optional.ofNullable(services.remove(service)))
                .ifPresent(userServicesManagementService::delete);

    }

    @Override
    public Map<User, Map<String, UserServices>> getAdminServicesMap() {
        return userServicesManagementService.getAdminServicesMap();
    }

    @Override
    public Set<String> getServicesNames(String role) {
        return configurationService.getServicesNames(role);

    }

    @Override
    public Map<String, ServiceInputField> getServicesInputFieldsMap(String service) {
        return configurationService.getServicesInputFieldsMap(service);
    }

    @Override
    public Map<String, ServiceSelectionListField> getServicesSelectionListFieldsMap(String service) {
        return configurationService.getServicesSelectionListFieldsMap(service);
    }

    @Override
    public Map<String, Set<String>> getServicesPrints(User user) {
        return translateService.getServicesPrints(user);
    }

    @Override
    public Optional<User> findUser(String id) {
        return findUser(UUID.fromString(id));
    }

    @Override
    public Optional<User> findUserByName(String name) {
        return userService.findByLogin(name);
    }

    @Override
    public Optional<User> findUser(UUID id) {
        return userService.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userService.getAll();
    }

    @Override
    public void deleteUser(UUID id) {
        userService.deleteById(id);
    }

    @Override
    public List<String> getAvailableRoles() {
        return configurationService.getRoles();
    }

    @Override
    public void changeRole(ChangeRoleDto dto) {
        userService.changeRole(dto);
    }

    @Override
    public void changeEnabledStatus(String id, boolean status) {
        log.debug("Toggle user {} 'enabled' status to {}", id, status);
        userService.changeEnabledStatus(UUID.fromString(id), status);
    }

    @Override
    public void forceLoginAttemptBlock(HttpServletRequest request) {
        String clientIP = UserDetailsServiceImpl.getClientIP(request);
        log.debug("Forcing login attempt block for {}", clientIP);
        loginAttemptService.forceLoginAttemptBlock(clientIP);
    }
}
