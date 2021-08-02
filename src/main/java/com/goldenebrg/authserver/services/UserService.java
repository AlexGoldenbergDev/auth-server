package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.rest.beans.PasswordResetForm;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.rest.beans.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Alex Goldenberg
 * Service for {@link InvitationToken} and {@link User} managment
 */
public interface UserService {

    List<String> getAvailableRoles();

    /**
     * Creates a new {@link User};
     *
     * @param userDto   - {@link UserDto} Sign In Form
     * @param requestId - request id
     * @return
     */


    List<User> getAll();


    void deleteById(UUID id);

    Optional<User> findById(UUID id);

    boolean isSignedUp(RequestForm requestForm);

    void changeRole(ChangeRoleDto dto);

    void changeEnabledStatus(UUID id, boolean status);


    Optional<User> resetPassword(PasswordResetToken token, PasswordResetForm form);

    Optional<User> findByLogin(String login);

    Optional<User> findUserByEmail(String email);

    User create(UserDto userDto, InvitationToken invitationToken);
}
