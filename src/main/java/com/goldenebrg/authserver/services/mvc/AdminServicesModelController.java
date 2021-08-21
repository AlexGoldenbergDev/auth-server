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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.goldenebrg.authserver.services.mvc.MvcControllerUtils.addInputFields;


@SuppressWarnings("SameParameterValue")
@Service
public class AdminServicesModelController {

    FacadeService facadeService;

    @Autowired
    AdminServicesModelController(FacadeService facadeService) {
        this.facadeService = facadeService;
    }

    @UserAccess
    public ModelAndView getAdminServicesPage(@NotNull ModelAndView modelAndView, @NotNull User user) {
        @NonNull String role = user.getRole();
        modelAndView.addObject("servicesMap", facadeService.getAdminServicesMap());
        modelAndView.addObject("services", facadeService.getServicesNames(role));
        return modelAndView;
    }


    @UserAccess
    public ModelAndView getAdminServicesModificationPage(@NotNull ModelAndView modelAndView,
                                                         @NotNull @NotEmpty String user,
                                                         @NotNull @NotEmpty String service,
                                                         @NotNull User currentUser) {

        Optional<User> targetUser = facadeService.findUser(user);

        @NonNull String role = currentUser.getRole();

        addInputFields(facadeService, modelAndView, service, role);

        Map<String, String> fields = targetUser.map(User::getUserServices)
                .map(map -> map.get(service))
                .map(UserServices::getFields)
                .map(f -> f.entrySet().stream()
                        .collect(Collectors
                                .toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue()))))
                .orElse(Collections.emptyMap());


        ServiceForm serviceForm = new ServiceForm();
        serviceForm.setFields(fields);
        serviceForm.setService(service);

        modelAndView.addObject("dto", serviceForm);
        modelAndView.addObject("service", service);
        modelAndView.addObject("user", user);

        return modelAndView;
    }


    @UserAccess
    public void createService(@NotNull @NotEmpty String user, @NotNull ServiceForm dto) {
        facadeService.createService(UUID.fromString(user), dto);
    }


    @UserAccess
    public void deleteService(@NotNull UUID id) {
        facadeService.deleteService(id);
    }
}
