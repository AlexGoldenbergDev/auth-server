package com.goldenebrg.authserver.services.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class AssignmentField implements Serializable {

    String name;

    @JsonProperty("default")
    String def;

    Set<String> options;


}
