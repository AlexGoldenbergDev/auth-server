package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.Request;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.rest.beans.UserDto;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * @author Alex Goldenberg
 * Service for {@link Request} and {@link User} managment
 */
public interface UserService {

    List<String> getAvailableRoles();

    /**
     * Creates a new Invitation {@link Request}
     */
    UUID createRequest(String email);

    /**
     * Validates presence of {@link Request} with following UUID
     * @param uuid - {@link Request#getId()}
     */
    boolean isRequestUUIDExists(UUID uuid);


    /**
     * Returns full {@link Request} list
     * @return List of all persisted requests
     */
    List<Request> getInvitations();

    /**
     *  {@link Request} Clean Up
     */
    void deleteOldRequest();

    /**
     * Deletes specific {@link Request}
     * @param uuid - {@link Request#getId()}
     */
    void deleteRequestById(UUID uuid);

    /**
     * Creates a new {@link User};
     * @param userDto - {@link UserDto} Sign In Form
     */
    void registerNewUserAccount(@NonNull UserDto userDto);


    List<User> getUsers();

    User getUser(String user);

    void deleteUserById(UUID id);

    User getUserById(UUID id);

    void changeRole(ChangeRoleDto dto);

    void toggleEnabledStatus(String id, boolean status);
}
