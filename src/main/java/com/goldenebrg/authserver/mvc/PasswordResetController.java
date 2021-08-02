package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.PasswordResetForm;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.services.FacadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static com.goldenebrg.authserver.mvc.MvcControllerUtils.doAutoLogin;

@Controller
@Slf4j
@RequestMapping("/reset")
public class PasswordResetController {

    private final AuthenticationManager authenticationManager;
    private final RootController rootController;
    private final FacadeService facadeService;


    @Autowired
    PasswordResetController(RootController rootController, AuthenticationManager authenticationManager, FacadeService facadeService) {
        this.rootController = rootController;
        this.authenticationManager = authenticationManager;
        this.facadeService = facadeService;
    }


    @GetMapping("/send")
    public ModelAndView passwordReset() {
        ModelAndView modelAndView = new ModelAndView("reset");
        modelAndView.addObject("resetForm", new RequestForm());
        return modelAndView;
    }


    @GetMapping("/{id}")
    public ModelAndView passwordReset(@PathVariable("id") UUID id) {

        return facadeService.findPasswordToken(id).map(token -> {
            ModelAndView modelAndView = new ModelAndView("resetForm");
            modelAndView.addObject("id", id);
            modelAndView.addObject("form", new PasswordResetForm());
            return modelAndView;
        }).orElse(new ModelAndView("index"));

    }


    @PostMapping("/{id}")
    public ModelAndView passwordReset(@PathVariable("id") UUID id,
                                      @ModelAttribute("form")
                                      @Valid PasswordResetForm form, BindingResult result, HttpServletRequest request) {
        ModelAndView modelAndView;
        List<String> messages = facadeService.validatePassword(form);
        if (!messages.isEmpty() || result.hasErrors()) {
            modelAndView = new ModelAndView("resetForm");
            modelAndView.addObject("passwordError", messages);
            return modelAndView;
        } else {
            facadeService.resetPassword(id, form).ifPresent(user -> doAutoLogin(authenticationManager, user.getUsername(), form.getPassword(), request));
            modelAndView = rootController.index();
        }
        return modelAndView;
    }


    @PostMapping("/send")
    public ModelAndView passwordReset(@ModelAttribute("resetForm") @Valid RequestForm resetForm) {
        facadeService.resetPassword(resetForm);
        return rootController.index();
    }
}
