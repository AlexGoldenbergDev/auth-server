package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.AssignmentsDao;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceServiceImpl implements ServiceService {


    private final AssignmentsDao assignmentsDao;
    private final UserDao userDao;
    private final ServerConfigurationService serverConfigurationService;

    @Autowired
    public ServiceServiceImpl(AssignmentsDao assignmentsDao, UserDao userDao, ServerConfigurationService serverConfigurationService) {
        this.assignmentsDao = assignmentsDao;
        this.userDao = userDao;
        this.serverConfigurationService = serverConfigurationService;
    }

    @Override
    public void create(User user, AssignmentForm dto) {
        UserAssignments userAssignments = Optional.ofNullable(user.getUserServices().get(dto.getAssignment()))
                .orElse(new UserAssignments());
        userAssignments.setName(dto.getAssignment());
        dto.getFields().forEach(userAssignments::addField);

        userAssignments.setUser(user);
        user.addUserService(userAssignments);

        userDao.save(user);
    }

    @Override
    public void delete(UUID id) {
        assignmentsDao.deleteById(id);
    }

    @Override
    public void delete(UserAssignments userAssignments) {
        assignmentsDao.delete(userAssignments);
    }

    @Override
    public Map<User, Map<String, UserAssignments>> getAdminAssignmentsMap() {

        String adminRole = serverConfigurationService.getAdminRole();
        return userDao.findAll().stream().collect(Collectors.toMap(user -> user, user -> user.getUserServices().entrySet().stream()
                .filter(entry -> serverConfigurationService.getAssignmentChangers(entry.getKey()).contains(adminRole))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

}
