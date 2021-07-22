package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.jpa.entities.PasswordResetToken;
import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.rest.beans.*;
import com.goldenebrg.authserver.security.auth.service.UserDetailsImpl;
import com.goldenebrg.authserver.services.AssignmentsService;
import com.goldenebrg.authserver.services.UserService;
import com.goldenebrg.authserver.services.config.AssignmentField;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
public class ViewController {

    private final UserService userService;
    private final AssignmentsService assignmentsService;
    private final AuthenticationManager authenticationManager;


    @Autowired
    ViewController(UserService userService, AssignmentsService assignmentsService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.assignmentsService = assignmentsService;
        this.authenticationManager = authenticationManager;
    }


    @GetMapping({ "/index", "/" })
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

    @GetMapping("/login/failed")
    public ModelAndView failedLogin() {
        ModelAndView modelAndView = index();
        modelAndView.addObject("isFailedLogin", true);
        return modelAndView;
    }


    @GetMapping("/reset/send")
    public ModelAndView resetRequest() {
        ModelAndView modelAndView = new ModelAndView("reset");
        modelAndView.addObject("resetForm", new RequestForm());
        return modelAndView;
    }

    @GetMapping("/reset/{id}")
    public ModelAndView resetRequest(@PathVariable("id") UUID id) {
        ModelAndView modelAndView;

        PasswordResetToken token = userService.getPasswordToken(id);

        if (token != null) {
            modelAndView = new ModelAndView("resetForm");
            modelAndView.addObject("id", id);
            modelAndView.addObject("form", new PasswordResetForm());
        }
        else
            modelAndView = new ModelAndView("index");

        return modelAndView;
    }

    @PostMapping("/reset/{id}")
    public ModelAndView resetRequest(@PathVariable("id") UUID id,
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
                    .ifPresent(user -> doAutoLogin(user.getUsername(), form.getPassword(), request));
            modelAndView = index();
        }
        return modelAndView;
    }


    @PostMapping("/reset/send")
    public ModelAndView resetRequest(@ModelAttribute("resetForm") @Valid RequestForm resetForm, BindingResult bindingResult) {
        userService.createPasswordReset(resetForm);
        return index();
    }


    @GetMapping("/signup/{invitation}")
    public ModelAndView signUp(@PathVariable String invitation) {
        ModelAndView modelAndView;

        try {
            UUID uuid = UUID.fromString(invitation);
            if (userService.isRequestUUIDExists(uuid)) {
                modelAndView = new ModelAndView("signup");
                modelAndView.addObject("user", new UserDto());
                modelAndView.addObject("uuid", invitation);
            }
            else modelAndView = getRejectUnknownPage();

        } catch (IllegalArgumentException e) {
            return getRejectUnknownPage();
        }

        return modelAndView;
    }

    @PostMapping("/signup")
    public ModelAndView registerUserAccount
            (@ModelAttribute("user") @Valid UserDto userDto,
             BindingResult bindingResult,
             @ModelAttribute("uuid") String uuid,
             HttpServletRequest request, Model model) {

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


    @GetMapping("/admin/users")
    public ModelAndView users() {
        ModelAndView modelAndView = new ModelAndView("users");
        modelAndView.addObject("users", userService.getSortedUsers());
        return modelAndView;
    }

    @DeleteMapping("/admin/users/{id}")
    public RedirectView users(@PathVariable("id") UUID id) {
        userService.deleteUserById(id);
        return new RedirectView("/admin/users");
    }

    @GetMapping("/admin/users/{id}/role")
    public ModelAndView role(@PathVariable("id") String id) {

        ModelAndView modelAndView = new ModelAndView("/role");
        modelAndView.addObject("id", id);
        modelAndView.addObject("roles", userService.getAvailableRoles());
        modelAndView.addObject("dto", new ChangeRoleDto());
        return modelAndView;
    }

    @PostMapping("/admin/users/{id}/role")
    public RedirectView role(@ModelAttribute("dto") ChangeRoleDto dto, @PathVariable("id") String id) {
        dto.setId(id);
        userService.changeRole(dto);
        return new RedirectView("/admin/users");
    }

    @PostMapping("/admin/users/{id}/enabled/{status}")
    public RedirectView toggleUserEnabledStatus(@PathVariable("id") String id, @PathVariable("status") boolean status) {
        userService.toggleEnabledStatus(id, status);
        return new RedirectView("/admin/users");
    }


    @GetMapping("/admin/invitations")
    public ModelAndView invitations() {
        ModelAndView modelAndView = new ModelAndView("invitations");
        modelAndView.addObject("invitations", userService.getSortedInvitations());
        modelAndView.addObject("requestForm", new RequestForm());
        return modelAndView;
    }

    @PostMapping("/admin/invitations")
    public ModelAndView addInvitation(@ModelAttribute("requestForm") @Valid RequestForm requestForm, BindingResult result, Model model) {

            ModelAndView modelAndView;
            if (result.hasErrors()) {
                modelAndView = new ModelAndView("invitations");
                modelAndView.addObject("invitations", userService.getSortedInvitations());
                modelAndView.addObject("requestForm", model.getAttribute("requestForm"));
            }
            else {
                userService.createInvitation(requestForm);
                modelAndView = invitations();
            }

            return modelAndView;

    }

    @DeleteMapping( "/admin/invitations/{id}" )
    public RedirectView deleteInvitation(@PathVariable UUID id) {
        userService.deleteRequestById(id);
        ModelAndView modelAndView = new ModelAndView("invitations");
        modelAndView.addObject("invitations", userService.getSortedInvitations());
        return new RedirectView("/admin/invitations");
    }

    @GetMapping("/admin/assignments")
    public ModelAndView assignmentsMain() {
        ModelAndView modelAndView = new ModelAndView("assignments");
        modelAndView.addObject("assignmentsMap", assignmentsService.getUsersAssignmentsMap());
        modelAndView.addObject("assignments", assignmentsService.getAllAssignmentsNames());
        return modelAndView;
    }

    @DeleteMapping("/admin/assignments/{id}")
    public RedirectView assignments(@PathVariable("id") UUID id) {
        assignmentsService.deleteById(id);
        return new RedirectView("/admin/assignments");
    }



    @GetMapping("/admin/assignments/master/{assignment}/{user}")
    public ModelAndView assignmentsPost(@PathVariable("assignment") String assignment, @PathVariable("user") String user) {
        ModelAndView modelAndView = new ModelAndView();

        Map<String, AssignmentField> fieldsOpt = assignmentsService.getAssignmentFieldsMap(assignment);
        if (!fieldsOpt.isEmpty()) {

            modelAndView = new ModelAndView("assignments_master2");
            modelAndView.addObject("dto", new AssignmentForm());
            modelAndView.addObject("fields", fieldsOpt);
            modelAndView.addObject("assignment", assignment);
            modelAndView.addObject("user", user);
        }

        return modelAndView;
    }

    @PostMapping("/admin/assignments/master/{assignment}/{user}")
    public RedirectView assignmentsPost(@PathVariable("assignment") String assignment, @PathVariable("user") String user, @ModelAttribute("dto")  AssignmentForm dto) {
        dto.setAssignment(assignment);
        assignmentsService.save(user, dto);
        return new RedirectView("/admin/assignments");

    }

    private static ModelAndView getRejectPage(String reason) {
        ModelAndView modelAndView = new ModelAndView("reject");
        modelAndView.addObject("reason", reason);
        return modelAndView;
    }


    private static ModelAndView getRejectUnknownPage() {
        return getRejectPage("Unknown Page");
    }


    private void doAutoLogin(String username, String password, HttpServletRequest request) {

        try {
            UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(username, password);
            Authentication auth = authenticationManager.authenticate(authReq);
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
        } catch (Exception e) {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }


}
