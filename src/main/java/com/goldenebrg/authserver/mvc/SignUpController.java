package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.LoginDto;
import com.goldenebrg.authserver.rest.beans.UserDto;
import com.goldenebrg.authserver.security.auth.service.UserDetailsImpl;
import com.goldenebrg.authserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static com.goldenebrg.authserver.mvc.MvcControllerUtils.getErrorPage;

@Controller
@RequestMapping("/signup")
public class SignUpController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;


    @Autowired
    SignUpController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/{invitation}")
    public ModelAndView signUp(@PathVariable String invitation) {
        ModelAndView modelAndView;

        try {
            UUID uuid = UUID.fromString(invitation);
            if (userService.isRequestUUIDExists(uuid)) {
                modelAndView = new ModelAndView("signup");
                modelAndView.addObject("user", new UserDto());
                modelAndView.addObject("uuid", invitation);
            } else modelAndView = getErrorPage(HttpStatus.NOT_FOUND, "Invitation doesn't exists");

        } catch (IllegalArgumentException e) {
            throw e;
        }

        return modelAndView;
    }


    @PostMapping("")
    public ModelAndView signUp
            (@ModelAttribute("user") @Valid UserDto userDto,
             BindingResult bindingResult,
             @ModelAttribute("uuid") String uuid,
             HttpServletRequest request) {

        ModelAndView modelAndView;

        try {
            UUID requestIid = UUID.fromString(uuid);
            List<String> passwordErrors = userService.getPasswordValidationErrors(userDto);
            List<String> loginErrors = userService.getLoginValidationErrors(userDto);


            if (!passwordErrors.isEmpty() || !loginErrors.isEmpty() || bindingResult.hasErrors()) {
                modelAndView = new ModelAndView("signup");
                modelAndView.addObject("passwordError", passwordErrors);
                modelAndView.addObject("loginError", loginErrors);
                modelAndView.addObject("user", userDto);
                modelAndView.addObject("uuid", requestIid);
            } else {
                modelAndView = new ModelAndView("index");
                userDto.setUuid(UUID.randomUUID());
                User user = userService.registerNewUserAccount(userDto, requestIid);
                modelAndView.addObject("user", userService.registerNewUserAccount(userDto, requestIid));
                Authentication authenticationToken = new UsernamePasswordAuthenticationToken(new UserDetailsImpl(user), userDto.getPassword());
                Authentication authenticate = authenticationManager.authenticate(authenticationToken);
                SecurityContext sc = SecurityContextHolder.getContext();
                sc.setAuthentication(authenticate);
                HttpSession session = request.getSession(true);
                session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
            }
        } catch (Exception uaeEx) {
            modelAndView = new ModelAndView("index");
            modelAndView.addObject("login", new LoginDto());

        }

        return modelAndView;
    }
}
