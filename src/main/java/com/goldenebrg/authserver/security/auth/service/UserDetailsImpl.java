package com.goldenebrg.authserver.security.auth.service;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import lombok.Data;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Data
public class UserDetailsImpl implements AppUserDetails {

    private final User user;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }

    private static AssignmentForm getAssignmentForm(UserAssignments userAssignments) {
        AssignmentForm assignmentForm = new AssignmentForm();
        assignmentForm.setAssignment(userAssignments.getName());

        Map<String, String> fields = new TreeMap<>();
        userAssignments.getFields().forEach((k, v) -> fields.put(k, String.valueOf(v)));
        assignmentForm.setFields(fields);
        return assignmentForm;
    }

    @Override
    public Map<String, Object> getClaims() {
        Map<String, Object> map = user.getUserServices().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> getAssignmentForm(entry.getValue())));

        @NonNull Boolean enabled = user.getEnabled();
        map.put("enabled", enabled);
        return map;
    }
}
