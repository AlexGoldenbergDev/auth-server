package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.AssignmentsDao;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.services.config.AssignmentInputField;
import com.goldenebrg.authserver.services.config.AssignmentJson;
import com.goldenebrg.authserver.services.config.AssignmentSelectionListField;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssignmentsServiceImpl implements AssignmentsService{

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
    public Set<String> getAllAssignmentsNames(@NonNull String role) {
        return assignmentsProperties.entrySet().stream()
                .filter(entry -> entry.getValue().getChangers().contains(role))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }


    @Override
    public Map<String, AssignmentSelectionListField> getAssignmentSelectionListFieldsMap(String assignment) {
        if (isAssignmentNotExists(assignment)) return Collections.emptyMap();
        return Optional.ofNullable(assignmentsProperties.get(assignment).getLists())
                .map(fields -> fields.stream().collect(Collectors.toMap(AssignmentSelectionListField::getName, field -> field))).orElse(Collections.emptyMap());
    }

    @Override
    public Map<String, AssignmentInputField> getAssignmentInputFieldsMap(String assignment) {
        if (isAssignmentNotExists(assignment)) return Collections.emptyMap();
        return Optional.ofNullable(assignmentsProperties.get(assignment).getInputs())
                .map(fields -> fields.stream().collect(Collectors.toMap(AssignmentInputField::getName, field -> field))).orElse(Collections.emptyMap());
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
    public Map<User, Map<String, UserAssignments>> getUsersAssignmentsMapForAdmin() {

        String adminRole = configurationService.getAdminRole();
        return userDao.findAll().stream().collect(Collectors.toMap(user -> user, user -> user.getUserServices().entrySet().stream()
                .filter(entry -> configurationService.getAssignmentChangers(entry.getKey()).contains(adminRole))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

    @Override
    public void save(String uuid, AssignmentForm dto) {
        userDao.findById(UUID.fromString(uuid))
                .ifPresent(user -> {
                    UserAssignments userAssignments = Optional.ofNullable(user.getUserServices().get(dto.getAssignment())).orElse(new UserAssignments());
                    userAssignments.setName(dto.getAssignment());
                    dto.getFields().forEach(userAssignments::addField);

                    userAssignments.setUser(user);
                    user.addUserService(userAssignments);

                    assignmentsDao.save(userAssignments);
                });
    }

    @Override
    public void deleteById(UUID id) {
        assignmentsDao.deleteById(id);
    }

    @Override
    public Map<String, Set<String>> getAssignmentPrints(User user) {
        Map<String, Set<String>> map = new TreeMap<>(Comparator.naturalOrder());
        @NonNull Map<String, UserAssignments> userServices = user.getUserServices();
        for (Map.Entry<String, UserAssignments> assignmentsEntry : userServices.entrySet()) {
            String name = assignmentsEntry.getKey();
            Map<String, AssignmentInputField> inputFieldsMap = getAssignmentInputFieldsMap(name);

            Set<String> set = Optional.ofNullable(map.get(name)).orElse(new TreeSet<>(Comparator.naturalOrder()));

            for (Map.Entry<String, Serializable> fieldsEntry : assignmentsEntry.getValue().getFields().entrySet()) {
                String field = fieldsEntry.getKey();

                String string = Optional.ofNullable(inputFieldsMap.get(field)).map(f -> {
                    String type = f.getType();
                    if ("password".equals(type)) {
                        return field + ": ***";
                    }
                    return field + ": " + fieldsEntry.getValue();
                }).orElse(field + ": " + fieldsEntry.getValue());

                set.add(string);
            }

            map.put(name, set);

        }

        return map;
    }

    @Override
    @Transactional
    public void deleteService(String user, String service) {
        Optional.ofNullable(userDao.findUserByUsername(user)).map(User::getUserServices)
                .flatMap(services -> Optional.ofNullable(services.remove(service)))
                .ifPresent(assignmentsDao::delete);
    }
}
