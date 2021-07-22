package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.util.UUID;

@Controller
@RequestMapping("/admin/invitations")
public class InvitationController {

    private final UserService userService;


    @Autowired
    InvitationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public ModelAndView invitations() {
        ModelAndView modelAndView = new ModelAndView("invitations");
        modelAndView.addObject("invitations", userService.getSortedInvitations());
        modelAndView.addObject("requestForm", new RequestForm());
        return modelAndView;
    }

    @PostMapping("")
    public ModelAndView addInvitation(@ModelAttribute("requestForm") @Valid RequestForm requestForm, BindingResult result, Model model) {

        ModelAndView modelAndView;
        if (result.hasErrors()) {
            modelAndView = new ModelAndView("invitations");
            modelAndView.addObject("invitations", userService.getSortedInvitations());
            modelAndView.addObject("requestForm", model.getAttribute("requestForm"));
        } else {

            boolean emailSignedUp = userService.isEmailSignedUp(requestForm);
            modelAndView = invitations();

            if (emailSignedUp) {
                modelAndView.addObject("emailError",
                        String.format("User for email '%s' already exists", requestForm.getEmail()));
            } else
                userService.createInvitation(requestForm);

        }

        return modelAndView;
    }

    @DeleteMapping("/{id}")
    public RedirectView deleteInvitation(@PathVariable UUID id) {
        userService.deleteRequestById(id);
        ModelAndView modelAndView = new ModelAndView("invitations");
        modelAndView.addObject("invitations", userService.getSortedInvitations());
        return new RedirectView("/admin/invitations");
    }

}
