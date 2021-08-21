package com.goldenebrg.authserver.services.config;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class ServicesConfiguration implements Serializable {

    Map<String, ServiceJson> services = new TreeMap<>();

    @JsonAnySetter
    void anySetter(String name, ServiceJson value) {
        services.put(name, value);
    }

    @JsonAnyGetter
    Map<String, ServiceJson> anyGetter(String name, ServiceJson value) {
        return services;
    }

    public Map<String, ServiceJson> getServices() {
        return services;
    }
}
