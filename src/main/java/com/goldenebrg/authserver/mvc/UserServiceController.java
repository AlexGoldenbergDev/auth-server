package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.ServiceForm;
import com.goldenebrg.authserver.services.mvc.UserServicesModelController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;


@Controller
@RequestMapping("/services")
public class UserServiceController {

    private final UserServicesModelController facadeService;


    @Autowired
    UserServiceController(UserServicesModelController facadeService) {
        this.facadeService = facadeService;
    }


    @PostMapping("/edit")
    public ModelAndView servicesPost(@ModelAttribute("serviceToAdd") ServiceForm serviceToAdd) {
        return facadeService.editService(new ModelAndView("services_master"), serviceToAdd.getService(), null);
    }


    @PostMapping("/edit/{service}")
    public ModelAndView servicesEdit(@PathVariable("service") String service) {
        return facadeService.editService(new ModelAndView("services_master"), service, null);
    }

    @PostMapping("/add/{service}")
    public RedirectView servicesPost(@PathVariable("service") String service,
                                     @ModelAttribute("dto") ServiceForm dto) {
        facadeService.createService(service, dto, null);
        return new RedirectView("/");

    }

    @DeleteMapping("/delete/{service}")
    public RedirectView servicesPost(@PathVariable("service") String service) {
        facadeService.deleteService(service, null);
        return new RedirectView("/");

    }

}
