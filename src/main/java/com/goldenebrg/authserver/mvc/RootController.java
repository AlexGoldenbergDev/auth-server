package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.mvc.exceptions.UnknownUserLoginAttempt;
import com.goldenebrg.authserver.rest.beans.LoginDto;
import com.goldenebrg.authserver.rest.beans.ServiceForm;
import com.goldenebrg.authserver.services.FacadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Controller
@Slf4j
public class RootController {

    private final FacadeService facadeService;


    @Autowired
    RootController(FacadeService facadeService) {
        this.facadeService = facadeService;
    }


    @GetMapping({"/index", "/"})
    public ModelAndView index(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("index");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Object userPrincipal = Optional.ofNullable(authentication)
                .map(Authentication::getPrincipal)
                .orElse("anonymousUser");


        if (!"anonymousUser".equals(userPrincipal)) {
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            Optional<User> user = facadeService.findUserByName(principal.getUsername());
            if (user.isPresent()) {
                User u = user.get();
                Map<String, Set<String>> services = facadeService.getServicesPrints(u);

                Set<String> available = facadeService.getServicesNames(u.getRole());
                available.removeAll(u.getUserServices().keySet());

                modelAndView.addObject("user", u);
                modelAndView.addObject("services", services);
                modelAndView.addObject("serviceToAdd", new ServiceForm());
                modelAndView.addObject("availableServices", available);
            } else {
                facadeService.forceLoginAttemptBlock(request);
                throw new UnknownUserLoginAttempt();
            }
        } else
            modelAndView.addObject("login", new LoginDto());


        return modelAndView;
    }


    @GetMapping("/login")
    public ModelAndView failedLogin(HttpServletRequest request, @RequestParam(name = "message") String message) {
        ModelAndView modelAndView = index(request);
        modelAndView.addObject("message", message);
        return modelAndView;
    }


}
