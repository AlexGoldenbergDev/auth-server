package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserServices;
import com.goldenebrg.authserver.rest.beans.ServiceForm;

import java.util.Map;
import java.util.UUID;

public interface UserServicesManagementService {
    void create(User user, ServiceForm dto);

    void delete(UUID id);

    void delete(UserServices userServices);

    Map<User, Map<String, UserServices>> getAdminServicesMap();
}
