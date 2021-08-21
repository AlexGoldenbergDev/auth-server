package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.ServiceForm;
import com.goldenebrg.authserver.services.mvc.AdminServicesModelController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {


    private final AdminServicesModelController facadeService;


    @Autowired
    AdminServiceController(AdminServicesModelController facadeService) {
        this.facadeService = facadeService;
    }


    @DeleteMapping("/{id}")
    public RedirectView services(@PathVariable("id") UUID id) {
        facadeService.deleteService(id);
        return new RedirectView("/admin/services/");
    }

    @GetMapping("/")
    public ModelAndView getAdminServicesPage() {
        ModelAndView modelAndView = new ModelAndView("services");
        return facadeService.getAdminServicesPage(modelAndView, null);

    }

    @GetMapping("/master/{service}/{user}")
    public ModelAndView getAdminServicesModificationPage(@PathVariable("service") String service, @PathVariable("user") String user) {

        ModelAndView modelAndView = new ModelAndView("services_master2");
        return facadeService.getAdminServicesModificationPage(modelAndView, user, service, null);
    }

    @PostMapping("/master/{user}")
    public RedirectView getAdminServicesModificationPage(@PathVariable("user") String user, @ModelAttribute("dto") ServiceForm dto) {
        facadeService.createService(user, dto);
        return new RedirectView("/admin/services/");
    }
}
