package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.services.config.AssignmentInputField;
import com.goldenebrg.authserver.services.config.AssignmentSelectionListField;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AssignmentsService {
    Set<String> getAllAssignmentsNames(@NonNull String role);

    Map<String, AssignmentSelectionListField> getAssignmentSelectionListFieldsMap(String assignment);

    Map<String, AssignmentInputField> getAssignmentInputFieldsMap(String assignment);

    boolean isAssignmentExists(String assignment);

    boolean isAssignmentNotExists(String assignment);


    Map<User, Map<String, UserAssignments>> getUsersAssignmentsMapForAdmin();

    void save(String user, AssignmentForm dto);

    void deleteById(UUID id);

    Map<String, Set<String>> getAssignmentPrints(User user);

    void deleteService(String user, String service);
}
