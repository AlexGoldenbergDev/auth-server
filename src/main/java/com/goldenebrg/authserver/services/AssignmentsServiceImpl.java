package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.AssignmentsDao;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.services.config.AssignmentField;
import com.goldenebrg.authserver.services.config.AssignmentJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssignmentsServiceImpl implements AssignmentsService{

    private static final String PATH = "/config.json";

    private final ServerConfigurationService configurationService;
    private final AssignmentsDao assignmentsDao;
    private final UserDao userDao;

    Map<String, AssignmentJson> assignmentsProperties;

    @Autowired
    AssignmentsServiceImpl(ServerConfigurationService configurationService, AssignmentsDao assignmentsDao, UserDao userDao) {
        this.configurationService = configurationService;
        this.assignmentsDao = assignmentsDao;
        this.userDao = userDao;
    }

    @PostConstruct
    void initialize() {
        this.assignmentsProperties = configurationService.getAssignments();
    }


    @Override
    public Set<String> getAllAssignmentsNames() {
        return assignmentsProperties.keySet();
    }

    @Override
    public Collection<AssignmentJson> getAllAssignments() {
        return assignmentsProperties.values();
    }

    @Override
    public Set<String> getAssignmentFieldsNames(String assignment) {
        if (isAssignmentNotExists(assignment)) return Collections.emptySet();
        return Optional.ofNullable(assignmentsProperties.get(assignment).getFields())
                .map(fields -> fields.stream().map(AssignmentField::getName)
                        .collect(Collectors.toSet())).orElse(Collections.emptySet());
    }

    @Override
    public AssignmentField getAssignmentFields(String assignment, String field) {
        if (isAssignmentNotExists(assignment)) return null;
        return Optional.ofNullable(assignmentsProperties.get(assignment).getFields())
                .flatMap(fields -> fields.stream().filter(f -> field.equals(f.getName()))
                .findAny()).orElse(null);

    }

    @Override
    public Map<String, AssignmentField> getAssignmentFieldsMap(String assignment) {
        if (isAssignmentNotExists(assignment)) return Collections.emptyMap();
        return Optional.ofNullable(assignmentsProperties.get(assignment).getFields())
                .map(fields -> fields.stream().collect(Collectors.toMap(AssignmentField::getName, field -> field))).orElse(Collections.emptyMap());
    }


    @Override
    public boolean isAssignmentExists(String assignment) {
        return assignmentsProperties.containsKey(assignment);
    }

    @Override
    public boolean isAssignmentNotExists(String assignment) {
        return !isAssignmentExists(assignment);
    }

    @Override
    public AssignmentJson getAssignment(String assignment) {
        return assignmentsProperties.get(assignment);
    }

    @Override
    public Map<User, Map<String, UserAssignments>> getUsersAssignmentsMap() {
        return userDao.findAll().stream().collect(Collectors.toMap(user -> user, User::getUserServices));
    }

    @Override
    public void save(String uuid, AssignmentForm dto) {
        userDao.findById(UUID.fromString(uuid))
                .ifPresent(user -> {
                    UserAssignments userAssignments = new UserAssignments();
                    userAssignments.setUser(user);
                    userAssignments.setName(dto.getAssignment());
                    dto.getFields().forEach(userAssignments::addField);
                    assignmentsDao.save(userAssignments);
                });
    }

    @Override
    public void deleteById(UUID id) {
        assignmentsDao.deleteById(id);
    }
}
