package com.goldenebrg.authserver.services.mvc;

import com.goldenebrg.authserver.rest.beans.PasswordResetForm;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.services.FacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.goldenebrg.authserver.services.mvc.MvcControllerUtils.doAutoLogin;

@Service
public class PasswordResetModelController {

    private final AuthenticationManager authenticationManager;
    FacadeService facadeService;

    @Autowired
    PasswordResetModelController(FacadeService facadeService,
                                 AuthenticationManager authenticationManager) {
        this.facadeService = facadeService;
        this.authenticationManager = authenticationManager;
    }


    public ModelAndView getPasswordResetForm(ModelAndView modelAndView) {
        modelAndView.addObject("resetForm", new RequestForm());
        return modelAndView;
    }


    public Optional<Object> resetPasswordPage(ModelAndView modelAndView, UUID id) {

        return facadeService.findPasswordToken(id).map(token -> {
            modelAndView.addObject("id", id);
            modelAndView.addObject("form", new PasswordResetForm());
            return modelAndView;
        });

    }


    public List<String> validatePassword(PasswordResetForm form) {
        return facadeService.validatePassword(form);
    }

    public void commitPasswordReset(UUID id, PasswordResetForm form, HttpServletRequest request) {
        facadeService.resetPassword(id, form).ifPresent(user -> doAutoLogin(authenticationManager, user.getUsername(), form.getPassword(), request));
    }

    public void createPasswordResetToken(RequestForm resetForm) {
        facadeService.resetPassword(resetForm);
    }

}
