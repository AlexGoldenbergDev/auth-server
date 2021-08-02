package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;

import java.util.Map;
import java.util.UUID;

public interface ServiceService {
    void create(User user, AssignmentForm dto);

    void delete(UUID id);

    void delete(User user, String service);

    void delete(UserAssignments userAssignments);

    Map<User, Map<String, UserAssignments>> getAdminAssignmentsMap();
}
