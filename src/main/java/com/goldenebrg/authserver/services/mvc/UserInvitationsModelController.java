package com.goldenebrg.authserver.services.mvc;

import com.goldenebrg.authserver.aspects.UserAccess;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.services.FacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Service
public class UserInvitationsModelController {

    FacadeService facadeService;

    @Autowired
    UserInvitationsModelController(FacadeService facadeService) {
        this.facadeService = facadeService;
    }


    @UserAccess
    public ModelAndView getInvitationsPage(@NotNull ModelAndView modelAndView) {
        modelAndView.addObject("invitations", facadeService.getAllInvitations());
        modelAndView.addObject("requestForm", new RequestForm());
        return modelAndView;
    }

    @UserAccess
    public ModelAndView getInvitationsPage(@NotNull ModelAndView modelAndView, @NotNull Object requestForm) {
        modelAndView.addObject("invitations", facadeService.getAllInvitations());
        modelAndView.addObject("requestForm", requestForm);
        return modelAndView;
    }


    @UserAccess
    public ModelAndView addInvitation(@NotNull ModelAndView modelAndView, @NotNull RequestForm requestForm) {

        modelAndView = getInvitationsPage(modelAndView);

        boolean emailSignedUp = facadeService.isSignedUp(requestForm);

        if (emailSignedUp) {
            modelAndView.addObject("emailError",
                    String.format("User for email '%s' already exists",
                            requestForm.getEmail()));
        } else
            facadeService.createInvitation(requestForm);

        return modelAndView;
    }

    @UserAccess
    public void deleteInvitation(UUID id) {
        facadeService.deleteInvitation(id);
    }
}
