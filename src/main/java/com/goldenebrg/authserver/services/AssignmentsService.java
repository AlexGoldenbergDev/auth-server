package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.services.config.AssignmentField;
import com.goldenebrg.authserver.services.config.AssignmentJson;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AssignmentsService {
    Set<String> getAllAssignmentsNames();

    Collection<AssignmentJson> getAllAssignments();

    Set<String> getAssignmentFieldsNames(String assignment);

    AssignmentField getAssignmentFields(String assignment, String field);

    Map<String, AssignmentField> getAssignmentFieldsMap(String assignment);

    boolean isAssignmentExists(String assignment);

    boolean isAssignmentNotExists(String assignment);

    AssignmentJson getAssignment(String assignment);

    Map<User, Map<String, UserAssignments>> getUsersAssignmentsMap();

    void save(String user, AssignmentForm dto);

    void deleteById(UUID id);
}
