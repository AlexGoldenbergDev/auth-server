package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.dao.ServicesDao;
import com.goldenebrg.authserver.jpa.dao.UserDao;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserServices;
import com.goldenebrg.authserver.rest.beans.ServiceForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @Caching(
            evict = {
                    @CacheEvict(value = "userById", key = "#user.id"),
                    @CacheEvict(value = "servicePrints", key = "#user.id"),
                    @CacheEvict(value = "userByLogin", key = "#user.username"),
                    @CacheEvict(value = "userByEmail", key = "#user.email"),
                    @CacheEvict(value = "adminServicesMap", allEntries = true),
                    @CacheEvict(value = {"allUsers"}, allEntries = true)
            })
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
    @Caching(
            evict = {
                    @CacheEvict(value = "userById", allEntries = true),
                    @CacheEvict(value = "servicePrints", allEntries = true),
                    @CacheEvict(value = "userByLogin", allEntries = true),
                    @CacheEvict(value = "userByEmail", allEntries = true),
                    @CacheEvict(value = "adminServicesMap", allEntries = true),
                    @CacheEvict(value = {"allUsers"}, allEntries = true)
            })
    public void delete(UUID id) {
        servicesDao.deleteById(id);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = "userById", key = "#userServices.user.id"),
                    @CacheEvict(value = "servicePrints", key = "#userServices.user.id"),
                    @CacheEvict(value = "userByLogin", key = "#userServices.user.username"),
                    @CacheEvict(value = "userByEmail", key = "#userServices.user.email"),
                    @CacheEvict(value = "adminServicesMap", allEntries = true),
                    @CacheEvict(value = {"allUsers"}, allEntries = true)
            })
    public void delete(UserServices userServices) {
        servicesDao.delete(userServices);
    }

    @Override
    @Cacheable(value = "adminServicesMap")
    public Map<User, Map<String, UserServices>> getAdminServicesMap() {

        String adminRole = serverConfigurationService.getAdminRole();
        return userDao.findAll().stream().collect(Collectors.toMap(user -> user, user -> user.getUserServices().entrySet().stream()
                .filter(entry -> serverConfigurationService.getServicesChangers(entry.getKey()).contains(adminRole))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

}
