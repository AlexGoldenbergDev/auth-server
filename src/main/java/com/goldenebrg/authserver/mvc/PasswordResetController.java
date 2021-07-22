package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.rest.beans.PasswordResetForm;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.services.UserService;
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
import java.util.Optional;
import java.util.UUID;

import static com.goldenebrg.authserver.mvc.MvcControllerUtils.doAutoLogin;

@Controller
@Slf4j
@RequestMapping("/reset")
public class PasswordResetController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final RootController rootController;


    @Autowired
    PasswordResetController(RootController rootController, UserService userService, AuthenticationManager authenticationManager) {
        this.rootController = rootController;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }


    @GetMapping("/send")
    public ModelAndView passwordReset() {
        ModelAndView modelAndView = new ModelAndView("reset");
        modelAndView.addObject("resetForm", new RequestForm());
        return modelAndView;
    }


    @GetMapping("/{id}")
    public ModelAndView passwordReset(@PathVariable("id") UUID id) {
        ModelAndView modelAndView;

        PasswordResetToken token = userService.getPasswordToken(id);

        if (token != null) {
            modelAndView = new ModelAndView("resetForm");
            modelAndView.addObject("id", id);
            modelAndView.addObject("form", new PasswordResetForm());
        } else modelAndView = new ModelAndView("index");
        return modelAndView;
    }


    @PostMapping("/{id}")
    public ModelAndView passwordReset(@PathVariable("id") UUID id,
                                      @ModelAttribute("form")
                                      @Valid PasswordResetForm form, BindingResult result, HttpServletRequest request) {
        ModelAndView modelAndView;
        List<String> messages = userService.getPasswordValidationErrors(form);
        if (!messages.isEmpty() || result.hasErrors()) {
            modelAndView = new ModelAndView("resetForm");
            modelAndView.addObject("passwordError", messages);
            return modelAndView;
        } else {
            Optional.ofNullable(userService.resetPassword(id, form))
                    .ifPresent(user -> doAutoLogin(authenticationManager, user.getUsername(), form.getPassword(), request));
            modelAndView = rootController.index();
        }
        return modelAndView;
    }


    @PostMapping("/send")
    public ModelAndView passwordReset(@ModelAttribute("resetForm") @Valid RequestForm resetForm) {
        userService.createPasswordReset(resetForm);
        return rootController.index();
    }
}
