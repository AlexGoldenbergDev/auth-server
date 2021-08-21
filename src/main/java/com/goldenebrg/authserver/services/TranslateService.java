package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserServices;
import com.goldenebrg.authserver.services.config.ServiceInputField;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

@Service
public class TranslateService {

    private final ServerConfigurationService configurationService;


    public TranslateService(ServerConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public Map<String, Set<String>> getServicesPrints(User user) {
        Map<String, Set<String>> map = new TreeMap<>(Comparator.naturalOrder());
        @NonNull Map<String, UserServices> userServices = user.getUserServices();
        for (Map.Entry<String, UserServices> servicesEntry : userServices.entrySet()) {
            String name = servicesEntry.getKey();
            Map<String, ServiceInputField> inputFieldsMap = configurationService.getServicesInputFieldsMap(name);

            Set<String> set = Optional.ofNullable(map.get(name)).orElse(new TreeSet<>(Comparator.naturalOrder()));

            for (Map.Entry<String, Serializable> fieldsEntry : servicesEntry.getValue().getFields().entrySet()) {
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
}
