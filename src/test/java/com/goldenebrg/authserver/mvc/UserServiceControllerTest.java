package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.index.ServiceName;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.ServiceForm;
import com.goldenebrg.authserver.security.auth.service.UserDetailsServiceImpl;
import com.goldenebrg.authserver.services.FacadeService;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.mvc.UserServicesModelController;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserServiceController.class)
@MockBean({
        ServerConfigurationService.class,
        UserServicesModelController.class,
        FacadeService.class,
        DataSource.class})
public class UserServiceControllerTest {


    private final MockMvc mvc;
    @MockBean(name = ServiceName.USER_DETAIL_SERVICE)
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    public UserServiceControllerTest(MockMvc mvc) {
        this.mvc = mvc;
    }


    @Order(1)
    @Test
    @WithMockUser("test")
    public void POST_edit_service() throws Exception {
        User mock = Mockito.mock(User.class);
        UUID uuid = UUID.randomUUID();
        when(mock.getId()).thenReturn(uuid);

        ServiceForm serviceForm = new ServiceForm();
        serviceForm.setService("service");
        mvc.perform(post("/services/edit/service").with(csrf())
                .flashAttr("serviceToAdd", serviceForm)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

        ;
    }


    @Order(1)
    @Test
    @WithMockUser("test")
    public void POST_add_service() throws Exception {
        User mock = Mockito.mock(User.class);
        UUID uuid = UUID.randomUUID();
        when(mock.getId()).thenReturn(uuid);

        ServiceForm serviceForm = new ServiceForm();
        serviceForm.setService("service");
        mvc.perform(post("/services/add/service").with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))


        ;
    }


    @Order(1)
    @Test
    @WithMockUser("test")
    public void DELETE_Service() throws Exception {
        User mock = Mockito.mock(User.class);
        UUID uuid = UUID.randomUUID();
        when(mock.getId()).thenReturn(uuid);

        ServiceForm serviceForm = new ServiceForm();
        serviceForm.setService("service");
        mvc.perform(delete("/services/delete/service").with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))


        ;
    }

}
