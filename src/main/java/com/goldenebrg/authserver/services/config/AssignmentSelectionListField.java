package com.goldenebrg.authserver.services.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssignmentSelectionListField extends AbstractAssignmentField {


    @JsonProperty("default")
    String def;


    Set<String> options;


}
