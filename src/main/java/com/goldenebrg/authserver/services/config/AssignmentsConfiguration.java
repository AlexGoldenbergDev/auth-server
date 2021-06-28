package com.goldenebrg.authserver.services.config;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class AssignmentsConfiguration implements Serializable {

    Map<String, AssignmentJson> assignments = new TreeMap<>();

    @JsonAnySetter
    void anySetter(String name, AssignmentJson value) {
        assignments.put(name, value);
    }

    @JsonAnyGetter
    Map<String, AssignmentJson> anyGetter(String name, AssignmentJson value) {
       return assignments;
    }

    public Map<String, AssignmentJson> getAssignments() {
        return assignments;
    }
}
