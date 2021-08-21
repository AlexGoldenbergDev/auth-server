package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.index.ServiceName;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.ServiceForm;
import com.goldenebrg.authserver.security.auth.service.UserDetailsServiceImpl;
import com.goldenebrg.authserver.services.FacadeService;
import com.goldenebrg.authserver.services.ServerConfigurationService;
import com.goldenebrg.authserver.services.mvc.AdminServicesModelController;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AdminServiceController.class)
@MockBean({
        ServerConfigurationService.class,
        AdminServicesModelController.class,
        FacadeService.class,
        DataSource.class})
public class AdminServiceControllerTest {


    private final MockMvc mvc;
    @MockBean(name = ServiceName.USER_DETAIL_SERVICE)
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    public AdminServiceControllerTest(MockMvc mvc) {
        this.mvc = mvc;
    }


    @Order(1)
    @Test
    @WithMockUser(value = "test", authorities = "ROLE_ADMIN")
    public void GET_services() throws Exception {
        User mock = Mockito.mock(User.class);
        UUID uuid = UUID.randomUUID();
        when(mock.getId()).thenReturn(uuid);

        ServiceForm serviceForm = new ServiceForm();
        serviceForm.setService("services");
        mvc.perform(get("/admin/services/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())


        ;
    }


    @Order(1)
    @Test
    @WithMockUser(value = "test", authorities = "ROLE_ADMIN")
    public void GET_edit_service() throws Exception {

        User mock = Mockito.mock(User.class);
        when(mock.getId()).thenReturn(UUID.randomUUID());

        ServiceForm serviceForm = new ServiceForm();
        serviceForm.setService("service");
        mvc.perform(get("/admin/services/master/test/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
        ;
    }


    @Order(1)
    @Test
    @WithMockUser(value = "test", authorities = "ROLE_ADMIN")
    public void POST_service() throws Exception {
        User mock = Mockito.mock(User.class);
        UUID uuid = UUID.randomUUID();
        when(mock.getId()).thenReturn(uuid);

        ServiceForm serviceForm = new ServiceForm();
        serviceForm.setService("test");

        mvc.perform(post("/admin/services/master/" + uuid.toString())
                .with(csrf())
                .flashAttr("dto", serviceForm)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(redirectedUrl("/admin/services"))


        ;
    }


    @Order(1)
    @Test
    @WithMockUser(value = "test", authorities = "ROLE_ADMIN")
    public void recognizedUser3() throws Exception {
        UUID uuid = UUID.randomUUID();

        User mock = Mockito.mock(User.class);
        when(mock.getId()).thenReturn(UUID.randomUUID());

        mvc.perform(delete("/admin/services/" + uuid.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(redirectedUrl("/admin/services"))


        ;
    }
}
