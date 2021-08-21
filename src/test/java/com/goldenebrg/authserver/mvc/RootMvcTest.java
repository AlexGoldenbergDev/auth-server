package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.index.ServiceName;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.mvc.exceptions.UnknownUserLoginAttempt;
import com.goldenebrg.authserver.rest.beans.LoginDto;
import com.goldenebrg.authserver.security.auth.service.UserDetailsServiceImpl;
import com.goldenebrg.authserver.services.FacadeService;
import com.goldenebrg.authserver.services.ServerConfigurationService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RootController.class)
@MockBean({
        ServerConfigurationService.class,
        FacadeService.class,
        DataSource.class})
public class RootMvcTest {

    private final FacadeService facadeService;
    private final MockMvc mvc;
    @MockBean(name = ServiceName.USER_DETAIL_SERVICE)
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    public RootMvcTest(FacadeService facadeService,
                       MockMvc mvc
    ) {
        this.facadeService = facadeService;
        this.mvc = mvc;
    }


    @Order(1)
    @Test
    public void Anon_GET_index() throws Exception {
        mvc.perform(get("/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute("login", new LoginDto()))
                .andExpect(model().attributeDoesNotExist("user"))

        ;
    }

    @Order(1)
    @Test
    @WithMockUser("test")
    public void Auth_GET_index() throws Exception {
        User mock = Mockito.mock(User.class);
        when(facadeService.findUserByName("test")).thenReturn(Optional.of(mock));

        mvc.perform(get("/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", mock))
                .andExpect(model().attributeDoesNotExist("login"))
        ;
    }

    @Order(2)
    @Test
    @WithMockUser("test")
    public void Unknown_GET_index() throws Exception {
        mvc.perform(get("/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnknownUserLoginAttempt))
        ;
    }
}
