package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.*;
import com.goldenebrg.authserver.services.config.AssignmentInputField;
import com.goldenebrg.authserver.services.config.AssignmentSelectionListField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FacadeServiceImpl implements FacadeService {

    private final InvitationService invitationService;
    private final PasswordResetService passwordResetService;
    private final UserService userService;
    private final LoginValidationService loginValidationService;
    private final PasswordValidationService passwordValidationService;
    private final ServiceService serviceService;
    private final ServerConfigurationService configurationService;
    private final TranslateService translateService;

    @Autowired
    public FacadeServiceImpl(InvitationService invitationService, PasswordResetService passwordResetService, UserService userService, LoginValidationService loginValidationService, PasswordValidationService passwordValidationService, ServiceService serviceService, ServerConfigurationService configurationService, TranslateService translateService) {
        this.invitationService = invitationService;
        this.passwordResetService = passwordResetService;
        this.userService = userService;
        this.loginValidationService = loginValidationService;
        this.passwordValidationService = passwordValidationService;
        this.serviceService = serviceService;
        this.configurationService = configurationService;
        this.translateService = translateService;
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
        invitationService.create(requestForm);
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
        Optional<PasswordResetToken> resetToken = passwordResetService.get(id);

        Optional<User> user = resetToken
                .flatMap(token -> userService.resetPassword(token, form));

        resetToken.ifPresent(passwordResetService::delete);

        return user;
    }

    @Override
    public Optional<User> signUp(UserDto userDto, UUID requestIid) {
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
    public void createService(String user, AssignmentForm dto) {
        userService.findByLogin(user).ifPresent(u -> serviceService.create(u, dto));
    }

    @Override
    public void deleteService(UUID id) {
        serviceService.delete(id);
    }

    @Override
    public void deleteService(String username, String service) {
        userService.findByLogin(username).map(User::getUserServices)
                .flatMap(services -> Optional.ofNullable(services.remove(service)))
                .ifPresent(serviceService::delete);

    }

    @Override
    public Map<User, Map<String, UserAssignments>> getAdminAssignmentsMap() {
        return serviceService.getAdminAssignmentsMap();
    }

    @Override
    public Set<String> getAssignmentsNames(String role) {
        return configurationService.getAssignmentsNames(role);

    }

    @Override
    public Map<String, AssignmentInputField> getAssignmentInputFieldsMap(String assignment) {
        return configurationService.getAssignmentInputFieldsMap(assignment);
    }

    @Override
    public Map<String, AssignmentSelectionListField> getAssignmentSelectionListFieldsMap(String assignment) {
        return configurationService.getAssignmentSelectionListFieldsMap(assignment);
    }

    @Override
    public Map<String, Set<String>> getAssignmentPrints(User user) {
        return translateService.getAssignmentPrints(user);
    }

    @Override
    public Optional<User> findUser(String id) {
        return findUser(UUID.fromString(id));
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
        userService.changeEnabledStatus(UUID.fromString(id), status);
    }
}
