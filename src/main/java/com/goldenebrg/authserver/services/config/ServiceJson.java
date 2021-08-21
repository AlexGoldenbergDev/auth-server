package com.goldenebrg.authserver.services.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class ServiceJson implements Serializable {

    @JsonProperty("isEnabled")
    Boolean isEnabled;
    Set<String> changers;
    Set<ServiceSelectionListField> lists;
    Set<ServiceInputField> inputs;

}
