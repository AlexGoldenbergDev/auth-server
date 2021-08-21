package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.index.ServiceName;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.security.auth.service.UserDetailsServiceImpl;
import com.goldenebrg.authserver.services.FacadeService;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.mvc.UserInvitationsModelController;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationController.class)
@MockBean({
        ServerConfigurationService.class,
        UserInvitationsModelController.class,
        FacadeService.class,
        DataSource.class})
public class InvitationControllerTest {


    private final MockMvc mvc;
    @MockBean(name = ServiceName.USER_DETAIL_SERVICE)
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    public InvitationControllerTest(MockMvc mvc) {
        this.mvc = mvc;
    }


    @Order(1)
    @Test
    @WithMockUser(value = "test", authorities = "ROLE_ADMIN")
    public void GET_invitations() throws Exception {
        User mock = Mockito.mock(User.class);
        UUID uuid = UUID.randomUUID();
        when(mock.getId()).thenReturn(uuid);

        mvc.perform(get("/admin/invitations/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())


        ;
    }
}
