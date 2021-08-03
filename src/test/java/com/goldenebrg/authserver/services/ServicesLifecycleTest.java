package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.rest.beans.UserDto;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@MockBean(ServerConfigurationService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class ServicesLifecycleTest {

    private static String email;
    private static UUID uuid;
    private static UUID serviceId;
    private final ServerConfigurationService serverConfigurationService;
    private final FacadeService facadeService;


    @Autowired
    public ServicesLifecycleTest(ServerConfigurationService serverConfigurationService, FacadeService facadeService) {
        this.serverConfigurationService = serverConfigurationService;
        this.facadeService = facadeService;
    }


    @Test
    @Order(1)
    void Given_Id_When_DeleteService_then_Doesnt_Throws() {
        assertThrows(EmptyResultDataAccessException.class, () -> facadeService.deleteService(UUID.randomUUID()));
    }

    @Test
    @Order(1)
    void Given_Name_When_DeleteService_then_Doesnt_Throws() {
        assertDoesNotThrow(() -> facadeService.deleteService("user", "service"));
    }

    @Test
    @Order(1)
    void When_getAdminAssignmentsMap_then_Empty() {
        assertThat(facadeService.getAdminAssignmentsMap()).isEmpty();
    }

    @Test
    @Order(1)
    void When_createService_then_Empty() {
        AssignmentForm assignmentForm = new AssignmentForm();
        assignmentForm.setAssignment("service");
        assignmentForm.setFields(Collections.singletonMap("filed", "test"));

        facadeService.createService("user", assignmentForm);

        assertThat(facadeService.getAdminAssignmentsMap()).isEmpty();
    }

    @Test
    @Order(2)
    void When_createService_then_Saved() {
        Mockito.when(serverConfigurationService.getHost()).thenReturn("localhost");
        Mockito.when(serverConfigurationService.getDefaultRole()).thenReturn("USER");
        Mockito.when(serverConfigurationService.getAdminRole()).thenReturn("ADMIN");
        Mockito.when(serverConfigurationService.getAssignmentChangers("service")).thenReturn(Collections.singleton("ADMIN"));

        email = "email@email.org";


        RequestForm requestForm = new RequestForm();
        requestForm.setEmail(email);

        assertDoesNotThrow(() -> facadeService.createInvitation(requestForm));

        uuid = UUID.randomUUID();
        String pass = "Pass";

        UserDto userDto = new UserDto();
        userDto.setUuid(uuid);
        userDto.setPassword(pass);
        userDto.setMatchingPassword(pass);
        userDto.setLogin("testUser");

        facadeService.signUp(userDto,
                facadeService.getAllInvitations().iterator().next().getId());


        AssignmentForm assignmentForm = new AssignmentForm();
        assignmentForm.setAssignment("service");
        assignmentForm.setFields(Collections.singletonMap("field", "test"));


        facadeService.createService("testUser", assignmentForm);


        Map<User, Map<String, UserAssignments>> adminAssignmentsMap = facadeService.getAdminAssignmentsMap();
        assertThat(adminAssignmentsMap).isNotEmpty();

        Map.Entry<User, Map<String, UserAssignments>> entry = adminAssignmentsMap.entrySet().iterator().next();

        User key = entry.getKey();
        assertEquals("testUser", key.getUsername());

        Map<String, UserAssignments> value = entry.getValue();
        assertThat(value).hasSize(1);

        UserAssignments userAssignments = value.values().iterator().next();
        assertEquals("service", userAssignments.getName());

        serviceId = userAssignments.getId();

        Map<String, Serializable> fields = userAssignments.getFields();
        assertThat(fields).hasSize(1);

        Map.Entry<String, Serializable> fieldEntry = fields.entrySet().iterator().next();
        assertEquals("field", fieldEntry.getKey());
        assertEquals("test", fieldEntry.getValue());

    }

    @Test
    @Order(3)
    void When_deleteById_then_Empty() {
        assertThrows(EmptyResultDataAccessException.class, () -> facadeService.deleteService(serviceId));
        assertThat(facadeService.getAdminAssignmentsMap()).isEmpty();
    }

    @Test
    @Order(4)
    void Given_Names_When_deleteService_then_Empty() {
        When_createService_then_Saved();
        assertDoesNotThrow(() -> facadeService.deleteService("testUser", "service"));
        List<UserAssignments> userAssignments = facadeService.getAdminAssignmentsMap().entrySet().stream()
                .flatMap(entry -> entry.getValue().values().stream())
                .collect(Collectors.toList());

        assertThat(userAssignments).isEmpty();
    }


}
