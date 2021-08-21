package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.ServicesDao;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserServices;
import com.goldenebrg.authserver.rest.beans.ServiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServicesManagementServiceImpl implements UserServicesManagementService {


    private final ServicesDao servicesDao;
    private final UserDao userDao;
    private final ServerConfigurationService serverConfigurationService;

    @Autowired
    public UserServicesManagementServiceImpl(ServicesDao servicesDao, UserDao userDao, ServerConfigurationService serverConfigurationService) {
        this.servicesDao = servicesDao;
        this.userDao = userDao;
        this.serverConfigurationService = serverConfigurationService;
    }

    @Override
    public void create(User user, ServiceForm dto) {
        UserServices userServices = Optional.ofNullable(user.getUserServices().get(dto.getService()))
                .orElse(new UserServices());
        userServices.setName(dto.getService());
        dto.getFields().forEach(userServices::addField);

        userServices.setUser(user);
        user.addUserService(userServices);

        userDao.save(user);
    }

    @Override
    public void delete(UUID id) {
        servicesDao.deleteById(id);
    }

    @Override
    public void delete(UserServices userServices) {
        servicesDao.delete(userServices);
    }

    @Override
    public Map<User, Map<String, UserServices>> getAdminServicesMap() {

        String adminRole = serverConfigurationService.getAdminRole();
        return userDao.findAll().stream().collect(Collectors.toMap(user -> user, user -> user.getUserServices().entrySet().stream()
                .filter(entry -> serverConfigurationService.getServicesChangers(entry.getKey()).contains(adminRole))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

}
