package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.rest.beans.PasswordResetForm;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.rest.beans.UserDto;
import com.sun.istack.NotNull;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * @author Alex Goldenberg
 * Service for {@link InvitationToken} and {@link User} managment
 */
public interface UserService {

    List<String> getAvailableRoles();

    /**
     * Creates a new Invitation {@link InvitationToken}
     */
    void createInvitation(@NotNull @Valid RequestForm email);

    void createPasswordReset(RequestForm requestForm);

    /**
     * Validates presence of {@link InvitationToken} with following UUID
     * @param uuid - {@link InvitationToken#getId()}
     */
    boolean isRequestUUIDExists(UUID uuid);


    /**
     * Returns full {@link InvitationToken} list
     * @return List of all persisted requests
     */
    List<InvitationToken> getInvitations();

    /**
     * Deletes specific {@link InvitationToken}
     * @param uuid - {@link InvitationToken#getId()}
     */
    void deleteRequestById(UUID uuid);

    /**
     * Creates a new {@link User};
     * @param userDto - {@link UserDto} Sign In Form
     * @param requestId - request id
     */
    User registerNewUserAccount(@NonNull UserDto userDto, UUID requestId);


    List<User> getUsers();


    void deleteUserById(UUID id);

    User getUserById(UUID id);

    void changeRole(ChangeRoleDto dto);

    void toggleEnabledStatus(String id, boolean status);

    PasswordResetToken getPasswordToken(UUID id);

    User resetPassword(UUID id, PasswordResetForm form);
}
