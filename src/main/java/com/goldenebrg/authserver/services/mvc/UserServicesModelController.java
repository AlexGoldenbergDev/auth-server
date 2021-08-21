package com.goldenebrg.authserver.services.mvc;

import com.goldenebrg.authserver.aspects.UserAccess;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserServices;
import com.goldenebrg.authserver.rest.beans.ServiceForm;
import com.goldenebrg.authserver.services.FacadeService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.goldenebrg.authserver.services.mvc.MvcControllerUtils.addInputFields;

@Service
public class UserServicesModelController {

    FacadeService facadeService;

    @Autowired
    UserServicesModelController(FacadeService facadeService) {
        this.facadeService = facadeService;
    }


    @UserAccess
    public ModelAndView editService(@NotNull ModelAndView modelAndView,
                                    @NotNull @NotEmpty String service,
                                    @NotNull User user) {

        @NonNull String role = user.getRole();
        addInputFields(facadeService, modelAndView, service, role);

        Map<String, Serializable> fields = Optional.ofNullable(user.getUserServices())
                .map(map -> map.get(service))
                .map(UserServices::getFields)
                .orElse(new HashMap<>());


        modelAndView.addObject("persistedField", fields);
        modelAndView.addObject("dto", new ServiceForm());
        modelAndView.addObject("service", service);
        modelAndView.addObject("user", user.getId().toString());


        return modelAndView;
    }


    @UserAccess
    public void createService(@NotNull @NotEmpty String service, @NotNull ServiceForm dto, @NotNull User user) {
        dto.setService(service);
        facadeService.createService(user.getId(), dto);
    }


    @UserAccess
    public void deleteService(@NotNull @NotEmpty String service, @NotNull User user) {
        facadeService.deleteService(user.getUsername(), service);
    }
}
