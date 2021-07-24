package com.goldenebrg.authserver.services.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssignmentInputField extends AbstractAssignmentField {
    String type;
}
