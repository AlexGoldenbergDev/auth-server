package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.UserDto;
import com.goldenebrg.authserver.services.mvc.SignUpControllerModelController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.goldenebrg.authserver.mvc.ViewUtils.getErrorPage;

@Controller
@RequestMapping("/signup")
public class SignUpController {

    private final SignUpControllerModelController facadeService;


    @Autowired
    SignUpController(SignUpControllerModelController facadeService) {
        this.facadeService = facadeService;
    }

    @GetMapping("/{invitation}")
    public ModelAndView signUp(@PathVariable String invitation) {
        ModelAndView modelAndView = new ModelAndView("signup");
        boolean invitationExists = facadeService.getSignUpPage(modelAndView, invitation);
        return invitationExists ? modelAndView : getErrorPage(HttpStatus.NOT_FOUND, "Invitation doesn't exists");
    }


    @PostMapping("/")
    public Object signUp
            (@ModelAttribute("user") @Valid UserDto userDto,
             BindingResult bindingResult,
             @ModelAttribute("uuid") UUID requestIid,
             HttpServletRequest request
            ) {

        Optional<ModelAndView> r = Optional.empty();


        try {
            List<String> passwordErrors = facadeService.getPasswordErrors(userDto);
            List<String> loginErrors = facadeService.getLoginErrors(userDto);

            if (!passwordErrors.isEmpty() || !loginErrors.isEmpty() || bindingResult.hasErrors()) {
                ModelAndView modelAndView = new ModelAndView("signup");
                modelAndView.addObject("passwordError", passwordErrors);
                modelAndView.addObject("loginError", loginErrors);
                modelAndView.addObject("user", userDto);
                modelAndView.addObject("uuid", requestIid);
                r = Optional.of(modelAndView);
            }

            return r.orElseGet(() -> {
                ModelAndView mav = new ModelAndView("index");
                return facadeService.signUp(mav, requestIid, userDto, request);
            });
        } catch (Exception uaeEx) {
            return new RedirectView("/");

        }
    }
}
