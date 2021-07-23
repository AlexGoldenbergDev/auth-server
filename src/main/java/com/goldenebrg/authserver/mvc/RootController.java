package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.LoginDto;
import com.goldenebrg.authserver.security.auth.service.UserDetailsImpl;
import com.goldenebrg.authserver.services.UserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
public class RootController {

    private final UserService userService;


    @Autowired
    RootController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping({"/index", "/"})
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Object userPrincipal = Optional.ofNullable(authentication)
                .map(Authentication::getPrincipal)
                .orElse("anonymousUser");

        if (!"anonymousUser".equals(userPrincipal)) {
            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            @NonNull UUID id = principal.getUser().getId();
            modelAndView.addObject("user", userService.getUserById(id));
        } else
            modelAndView.addObject("login", new LoginDto());


        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView failedLogin(@RequestParam(name = "message") String message) {
        ModelAndView modelAndView = index();
        modelAndView.addObject("message", message);
        return modelAndView;
    }


}
