package com.goldenebrg.authserver.services.config;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class AbstractAssignmentField implements Serializable {

    String name;
    Set<String> changers;
}
