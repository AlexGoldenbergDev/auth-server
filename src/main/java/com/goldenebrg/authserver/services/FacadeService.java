package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserServices;
import com.goldenebrg.authserver.rest.beans.*;
import com.goldenebrg.authserver.services.config.ServiceInputField;
import com.goldenebrg.authserver.services.config.ServiceSelectionListField;

import javax.servlet.http.HttpServletRequest;
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

    void createService(UUID id, ServiceForm dto);

    void deleteService(UUID id);

    void deleteService(String username, String service);

    Map<User, Map<String, UserServices>> getAdminServicesMap();

    Set<String> getServicesNames(String role);

    Map<String, ServiceInputField> getServicesInputFieldsMap(String service);

    Map<String, ServiceSelectionListField> getServicesSelectionListFieldsMap(String service);

    Map<String, Set<String>> getServicesPrints(User u);

    Optional<User> findUser(String id);

    Optional<User> findUserByName(String name);

    Optional<User> findUser(UUID id);

    List<User> getAllUsers();

    void deleteUser(UUID id);

    List<String> getAvailableRoles();

    void changeRole(ChangeRoleDto dto);

    void changeEnabledStatus(String id, boolean status);

    void forceLoginAttemptBlock(HttpServletRequest request);
}
