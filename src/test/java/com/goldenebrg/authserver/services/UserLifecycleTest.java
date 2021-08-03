package com.goldenebrg.authserver.services;

import com.goldenebrg.authserver.jpa.entities.InvitationToken;
import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.rest.beans.PasswordResetForm;
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
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@MockBean(ServerConfigurationService.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserLifecycleTest {


    private static UUID inviteId;
    private static UUID uuid;
    private static String email;
    private static UUID passwordResetId;

    private final ServerConfigurationService serverConfigurationService;
    private final FacadeService facadeService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    UserLifecycleTest(ServerConfigurationService serverConfigurationService,
                      FacadeService facadeService,
                      PasswordEncoder passwordEncoder) {
        this.serverConfigurationService = serverConfigurationService;
        this.facadeService = facadeService;
        this.passwordEncoder = passwordEncoder;
    }

    @Test
    @Order(1)
    void IsInitialized() {
        assertNotNull(facadeService);
    }

    @Test
    @Order(1)
    void When_UserNotExists_then_Empty() {
        UUID uuid = UUID.randomUUID();
        Optional<User> user = facadeService.findUser(uuid);
        assertThat(user).isEmpty();
    }

    @Test
    @Order(1)
    void When_DeleteUserNotExists_then_NotThrow() {
        UUID uuid = UUID.randomUUID();
        assertThrows(EmptyResultDataAccessException.class, () -> facadeService.deleteUser(uuid));
    }

    @Test
    @Order(1)
    void When_GetNotExistsUsers_then_Empty() {
        Collection<User> sortedUsers = facadeService.getAllUsers();
        assertThat(sortedUsers).isEmpty();
    }


    @Test
    @Order(1)
    void When_GetNotExistsInvitations_then_Empty() {
        List<InvitationToken> invitations = facadeService.getAllInvitations();
        assertThat(invitations).isEmpty();
    }

    @Test
    @Order(1)
    void When_GetPasswordRequestTokenNotExists_then_Empty() {
        UUID uuid = UUID.randomUUID();
        Optional<PasswordResetToken> passwordResetToken = facadeService.findPasswordToken(uuid);
        assertThat(passwordResetToken).isEmpty();
    }

    @Test
    @Order(1)
    void When_GetRoles_then_ReturnList() {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        Mockito.when(serverConfigurationService.getRoles()).thenReturn(roles);
        assertIterableEquals(roles, facadeService.getAvailableRoles());

        roles = Collections.singletonList("USER");
        Mockito.when(serverConfigurationService.getRoles()).thenReturn(roles);
        assertIterableEquals(roles, facadeService.getAvailableRoles());

    }

    @Test
    @Order(1)
    void When_ChangeRoleUserNotExists_then_DoesntThrow() {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        Mockito.when(serverConfigurationService.getRoles()).thenReturn(roles);


        ChangeRoleDto changeRoleDto = new ChangeRoleDto();
        UUID uuid = UUID.randomUUID();
        changeRoleDto.setId(uuid.toString());
        changeRoleDto.setRole("USER");
        assertDoesNotThrow(() -> facadeService.changeRole(changeRoleDto));
    }

    @Test
    @Order(1)
    void When_ToggleEnabledStatus_then_DoesntThrow() {
        assertDoesNotThrow(() -> facadeService.changeEnabledStatus(UUID.randomUUID().toString(), false));
    }

    @Test
    @Order(1)
    void When_ChangePassTokenNotExists_then_Empty() {


        PasswordResetForm passwordResetForm = new PasswordResetForm();
        passwordResetForm.setPassword("123");
        passwordResetForm.setMatchingPassword("123");

        UUID uuid = UUID.randomUUID();

        assertThat(facadeService.resetPassword(uuid, passwordResetForm)).isEmpty();
        assertThat(facadeService.findPasswordToken(uuid)).isEmpty();
    }

    @Test
    @Order(1)
    void When_isEmailSignedUp_then_False() {

        RequestForm requestForm = new RequestForm();
        requestForm.setEmail("email");
        assertFalse(facadeService.isSignedUp(requestForm));
    }


    @Test
    @Order(1)
    void Given_InvalidRecipientAddress_When_CreateInvitation_then_Exception() {
        Mockito.when(serverConfigurationService.getHost()).thenReturn("localhost");

        String email = "email";
        RequestForm requestForm = new RequestForm();
        requestForm.setEmail(email);

        assertThrows(MailSendException.class, () -> facadeService.createInvitation(requestForm));

        List<InvitationToken> invitations = facadeService.getAllInvitations();
        assertThat(invitations).isEmpty();
    }

    @Test
    @Order(1)
    void When_SignUp_then_Empty() {


        UserDto userDto = new UserDto();
        userDto.setUuid(UUID.randomUUID());
        userDto.setPassword("Pass");
        userDto.setMatchingPassword("Pass");
        userDto.setLogin("testUser");

        Optional<User> user = facadeService.signUp(userDto, UUID.randomUUID());

        assertThat(user).isEmpty();
        assertThat(facadeService.getAllUsers()).isEmpty();
    }

    @Test
    @Order(1)
    void When_CreatePasswordReset_then_Empty() {
        RequestForm requestForm = new RequestForm();
        requestForm.setEmail("email@email.org");
        Optional<PasswordResetToken> passwordReset = facadeService.resetPassword(requestForm);
        assertThat(passwordReset).isEmpty();

    }

    @Test
    @Order(2)
    void Given_ValidRecipientAddress_When_CreateInvitation_then_Invite() {
        Mockito.when(serverConfigurationService.getHost()).thenReturn("localhost");

        String email = "email@email.org";
        RequestForm requestForm = new RequestForm();
        requestForm.setEmail(email);

        assertDoesNotThrow(() -> facadeService.createInvitation(requestForm));

        List<InvitationToken> invitations = facadeService.getAllInvitations();
        InvitationToken invitationToken = invitations.get(0);
        inviteId = invitationToken.getId();
        assertThat(invitations).isNotEmpty();
        assertEquals(email, invitations.get(0).getEmail());
    }

    @Test
    @Order(3)
    void When_isInvitationExists_then_True() {
        assertTrue(facadeService.isInvitationExists(inviteId));
    }

    @Test
    @Order(4)
    void Given_NonConfiguredDefaultRole_When_SignUp_then_User() {

        UUID uuid = UUID.randomUUID();
        String pass = "Pass";

        UserDto userDto = new UserDto();
        userDto.setUuid(uuid);
        userDto.setPassword(pass);
        userDto.setMatchingPassword(pass);
        userDto.setLogin("testUser");

        assertThrows(NullPointerException.class, () -> facadeService.signUp(userDto, inviteId));


    }

    @Test
    @Order(5)
    void When_SignUp_then_User() {

        Mockito.when(serverConfigurationService.getDefaultRole()).thenReturn("USER");

        email = "email@email.org";
        uuid = UUID.randomUUID();
        String pass = "Pass";

        UserDto userDto = new UserDto();
        userDto.setUuid(uuid);
        userDto.setPassword(pass);
        userDto.setMatchingPassword(pass);
        userDto.setLogin("testUser");

        Optional<User> user = facadeService.signUp(userDto, inviteId);
        assertThat(user).isNotEmpty();
    }

    @Test
    @Order(6)
    void When_getUsers_then_HasOne() {
        List<User> users = facadeService.getAllUsers();
        User user = users.get(0);
        String pass = "Pass";

        assertEquals(1, users.size());
        assertThat(user).hasFieldOrPropertyWithValue("email", email);
        assertThat(user).hasFieldOrPropertyWithValue("id", uuid);
        assertThat(user).hasFieldOrPropertyWithValue("role", "USER");
        assertThat(user).hasFieldOrPropertyWithValue("enabled", true);
        assertTrue(passwordEncoder.matches(pass, user.getPassword()));
    }


    @Test
    @Order(6)
    void When_isEmailSignedUp_then_True() {

        RequestForm requestForm = new RequestForm();
        requestForm.setEmail("email@email.org");
        assertTrue(facadeService.isSignedUp(requestForm));
    }

    @Test
    @Order(6)
    void When_ChangeRoleToUnknown_then_NoChanges() {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        Mockito.when(serverConfigurationService.getRoles()).thenReturn(roles);

        ChangeRoleDto changeRoleDto = new ChangeRoleDto();
        changeRoleDto.setId(uuid.toString());
        changeRoleDto.setRole("UNKNOWN");

        assertDoesNotThrow(() -> facadeService.changeRole(changeRoleDto));

        Optional<User> optionalUser = facadeService.findUser(uuid);
        assertTrue(optionalUser.isPresent());

        User user = optionalUser.get();
        assertThat(user).hasFieldOrPropertyWithValue("role", "USER");
    }

    @Test
    @Order(6)
    void When_toggleEnabledStatus_then_Changes() {

        assertDoesNotThrow(() -> facadeService.changeEnabledStatus(uuid.toString(), false));

        Optional<User> optionalUser = facadeService.findUser(uuid);
        assertTrue(optionalUser.isPresent());

        User user = optionalUser.get();
        assertThat(user).hasFieldOrPropertyWithValue("enabled", false);

        assertDoesNotThrow(() -> facadeService.changeEnabledStatus(uuid.toString(), true));

        optionalUser = facadeService.findUser(uuid);
        assertTrue(optionalUser.isPresent());

        user = optionalUser.get();
        assertThat(user).hasFieldOrPropertyWithValue("enabled", true);
    }

    @Test
    @Order(7)
    void When_ChangeRoleToKnown_then_Change() {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        Mockito.when(serverConfigurationService.getRoles()).thenReturn(roles);

        ChangeRoleDto changeRoleDto = new ChangeRoleDto();
        changeRoleDto.setId(uuid.toString());
        changeRoleDto.setRole("ADMIN");

        assertDoesNotThrow(() -> facadeService.changeRole(changeRoleDto));

        Optional<User> optionalUser = facadeService.findUser(uuid);
        assertTrue(optionalUser.isPresent());

        User user = optionalUser.get();
        assertThat(user).hasFieldOrPropertyWithValue("role", "ADMIN");
    }

    @Test
    @Order(8)
    void When_CreatePasswordReset_then_Create() {
        Mockito.when(serverConfigurationService.getHost()).thenReturn("localhost");

        RequestForm requestForm = new RequestForm();
        requestForm.setEmail(email);

        Optional<PasswordResetToken> optionalPasswordResetToken = facadeService.resetPassword(requestForm);
        assertTrue(optionalPasswordResetToken.isPresent());

        PasswordResetToken passwordResetToken = optionalPasswordResetToken.get();
        passwordResetId = passwordResetToken.getId();
        assertThat(passwordResetToken).hasFieldOrPropertyWithValue("email", email);
    }

    @Test
    @Order(9)
    void When_resetPassword_then_Reset() {
        String password = "Password";
        PasswordResetForm passwordResetForm = new PasswordResetForm();
        passwordResetForm.setPassword(password);
        passwordResetForm.setMatchingPassword(password);

        Optional<User> optionalUser = facadeService.resetPassword(passwordResetId, passwordResetForm);
        assertTrue(optionalUser.isPresent());

        User user = optionalUser.get();
        assertTrue(passwordEncoder.matches(password, user.getPassword()));

    }

    @Test
    @Order(10)
    void When_createInvitation_then_NotEmpty() {
        Mockito.when(serverConfigurationService.getHost()).thenReturn("localhost");

        String email = "email@email.org";
        RequestForm requestForm = new RequestForm();
        requestForm.setEmail(email);

        assertDoesNotThrow(() -> facadeService.createInvitation(requestForm));
        assertThat(facadeService.getAllInvitations()).isEmpty();

    }

    @Test
    @Order(11)
    void When_deleteUser_then_Empty() {
        assertDoesNotThrow(() -> facadeService.deleteUser(uuid));
        assertThat(facadeService.getAllUsers()).isEmpty();

    }

    @Test
    @Order(12)
    void When_DeleteInvitation_then_Empty() {
        Mockito.when(serverConfigurationService.getHost()).thenReturn("localhost");

        String email = "email@email.org";
        RequestForm requestForm = new RequestForm();
        requestForm.setEmail(email);

        assertDoesNotThrow(() -> facadeService.createInvitation(requestForm));
        InvitationToken invitationToken = facadeService.getAllInvitations().iterator().next();
        inviteId = invitationToken.getId();
        assertThat(facadeService.getAllInvitations()).isNotEmpty();

        assertDoesNotThrow(() -> facadeService.deleteInvitation(inviteId));
        assertThat(facadeService.getAllInvitations()).isEmpty();

    }

}