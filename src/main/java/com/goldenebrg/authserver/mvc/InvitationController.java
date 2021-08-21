package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.services.mvc.UserInvitationsModelController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.util.UUID;


@Controller
@RequestMapping("/admin/invitations")
public class InvitationController {

    private final UserInvitationsModelController facadeService;


    @Autowired
    InvitationController(UserInvitationsModelController facadeService) {
        this.facadeService = facadeService;
    }

    @GetMapping("/")
    public ModelAndView invitations() {
        return facadeService.getInvitationsPage(new ModelAndView("invitations"));
    }

    @PostMapping("/")
    public ModelAndView addInvitation(@ModelAttribute("requestForm") @Valid RequestForm requestForm, BindingResult result) {
        ModelAndView modelAndView = new ModelAndView("invitations");
        if (result.hasErrors())
            return facadeService.getInvitationsPage(modelAndView, requestForm);
        else
            return facadeService.addInvitation(modelAndView, requestForm);

    }

    @DeleteMapping("/{id}")
    public RedirectView deleteInvitation(@PathVariable UUID id) {
        facadeService.deleteInvitation(id);
        return new RedirectView("/admin/invitations/");
    }

}
