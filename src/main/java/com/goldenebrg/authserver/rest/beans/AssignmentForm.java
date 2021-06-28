package com.goldenebrg.authserver.rest.beans;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class AssignmentForm implements Serializable {

    String assignment;
    Map<String, String> fields = new HashMap<>();
}
