package com.goldenebrg.authserver.services.mvc;

import com.goldenebrg.authserver.rest.beans.LoginDto;
import com.goldenebrg.authserver.rest.beans.UserDto;
import com.goldenebrg.authserver.services.FacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static com.goldenebrg.authserver.services.mvc.MvcControllerUtils.doAutoLogin;

@Service
@SuppressWarnings("SameParameterValue")
public class SignUpControllerModelController {

    private final FacadeService facadeService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    SignUpControllerModelController(FacadeService facadeService, AuthenticationManager authenticationManager) {
        this.facadeService = facadeService;
        this.authenticationManager = authenticationManager;
    }


    public boolean getSignUpPage(ModelAndView modelAndView, String invitation) {
        UUID uuid = UUID.fromString(invitation);
        boolean invitationExists = facadeService.isInvitationExists(uuid);
        if (facadeService.isInvitationExists(uuid)) {
            modelAndView.addObject("user", new UserDto());
            modelAndView.addObject("uuid", invitation);
        }
        return invitationExists;
    }

    public List<String> getLoginErrors(UserDto userDto) {

        return facadeService.validateLogin(userDto);
    }

    public List<String> getPasswordErrors(UserDto userDto) {
        return facadeService.validatePassword(userDto);
    }


    public ModelAndView signUp
            (ModelAndView mav,
             UUID requestIid,
             UserDto userDto,
             HttpServletRequest request) {

        userDto.setUuid(UUID.randomUUID());
        return facadeService.signUp(userDto, requestIid).map(user -> {
            mav.addObject("user", user);
            doAutoLogin(authenticationManager, user.getUsername(), user.getPassword(), request);
            return mav;
        }).orElseGet(() -> {
            mav.addObject("login", new LoginDto());
            return mav;
        });
    }
}
