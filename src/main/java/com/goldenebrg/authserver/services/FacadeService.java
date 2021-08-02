package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.*;
import com.goldenebrg.authserver.services.config.AssignmentInputField;
import com.goldenebrg.authserver.services.config.AssignmentSelectionListField;

import java.util.*;

public interface FacadeService {
    boolean isInvitationExists(UUID uuid);

    List<InvitationToken> getAllInvitations();

    void createInvitation(RequestForm requestForm);

    void deleteInvitation(UUID id);

    boolean isSignedUp(RequestForm requestForm);

    List<String> validatePassword(PasswordInputForm form);

    Optional<PasswordResetToken> resetPassword(RequestForm resetForm);

    Optional<PasswordResetToken> findPasswordToken(UUID id);

    Optional<User> resetPassword(UUID id, PasswordResetForm form);

    Optional<User> signUp(UserDto userDto, UUID requestIid);

    List<String> validateLogin(UserDto userDto);

    void createService(String user, AssignmentForm dto);

    void deleteService(UUID id);

    void deleteService(String username, String service);

    Map<User, Map<String, UserAssignments>> getAdminAssignmentsMap();

    Set<String> getAssignmentsNames(String role);

    Map<String, AssignmentInputField> getAssignmentInputFieldsMap(String assignment);

    Map<String, AssignmentSelectionListField> getAssignmentSelectionListFieldsMap(String assignment);

    Map<String, Set<String>> getAssignmentPrints(User u);

    Optional<User> findUser(String id);

    Optional<User> findUser(UUID id);

    List<User> getAllUsers();

    void deleteUser(UUID id);

    List<String> getAvailableRoles();

    void changeRole(ChangeRoleDto dto);

    void changeEnabledStatus(String id, boolean status);
}
