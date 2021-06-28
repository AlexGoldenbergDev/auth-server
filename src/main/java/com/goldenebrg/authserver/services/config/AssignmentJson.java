package com.goldenebrg.authserver.services.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class AssignmentJson implements Serializable {

    @JsonProperty("isEnabled")
    Boolean isEnabled;
    Set<AssignmentField> fields;

}
