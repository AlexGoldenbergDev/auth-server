package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.PasswordResetForm;
import com.goldenebrg.authserver.rest.beans.RequestForm;
import com.goldenebrg.authserver.services.mvc.PasswordResetModelController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;


@Controller
@Slf4j
@RequestMapping("/reset")
public class PasswordResetController {
    private final PasswordResetModelController facadeService;

    @Autowired
    PasswordResetController(PasswordResetModelController facadeService) {

        this.facadeService = facadeService;
    }


    @GetMapping("/send")
    public ModelAndView passwordReset() {
        return facadeService.getPasswordResetForm(new ModelAndView("reset"));
    }


    @GetMapping("/{id}")
    public Object passwordReset(@PathVariable("id") UUID id) {
        return facadeService.resetPasswordPage(new ModelAndView("resetForm"), id)
                .orElse(new RedirectView("/"));
    }


    @PostMapping("/{id}")
    public Object passwordReset(
            @PathVariable("id") UUID id,
            @ModelAttribute("form")
            @Valid PasswordResetForm form,
            HttpServletRequest request,
            BindingResult result
    ) {
        List<String> messages = facadeService.validatePassword(form);
        if (!messages.isEmpty() || result.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("resetForm");
            modelAndView.addObject("passwordError", messages);
            return modelAndView;
        } else {
            facadeService.commitPasswordReset(id, form, request);
            return new RedirectView("/");
        }
    }


    @PostMapping("/send")
    public RedirectView passwordReset(@ModelAttribute("resetForm") @Valid RequestForm resetForm) {
        facadeService.createPasswordResetToken(resetForm);
        return new RedirectView("/");
    }
}
