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
import java.util.Map;
import java.util.UUID;

@Controller
@Slf4j
public class ViewController {

    private final UserService userService;
    private final AssignmentsService assignmentsService;


    @Autowired
    ViewController(UserService userService, AssignmentsService assignmentsService) {
        this.userService = userService;
        this.assignmentsService = assignmentsService;
    }


    @GetMapping({ "/index", "/" })
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (!"anonymousUser".equals(authentication.getPrincipal())) {
            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            @NonNull UUID id = principal.getUser().getId();
            modelAndView.addObject("user", userService.getUserById(id));
        }
        else
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
    public RedirectView resetRequest(@PathVariable("id") UUID id, @ModelAttribute("form") PasswordResetForm form, HttpServletRequest request) {
        User user = userService.resetPassword(id, form);
        RedirectView redirectView = new RedirectView("index");
        if (user != null) {
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authenticationToken);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
        }

        return redirectView;
    }

    @PostMapping("/reset/send")
    public ModelAndView resetRequest(@ModelAttribute("resetForm") RequestForm resetForm) {
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
            (@ModelAttribute("user")  UserDto userDto,
             @ModelAttribute("uuid")  String uuid,
             HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView("index");
        try {
            UUID requestIid = UUID.fromString(uuid);
            userDto.setUuid(UUID.randomUUID());
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(userDto.getLogin(), userDto.getPassword());
            modelAndView.addObject("user", userService.registerNewUserAccount(userDto, requestIid));
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authenticationToken);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
        } catch (Exception uaeEx) {
            modelAndView.addObject("login", new LoginDto());

        }

        return modelAndView;
    }

    @GetMapping("/admin/users")
    public ModelAndView users() {
        ModelAndView modelAndView = new ModelAndView("users");
        modelAndView.addObject("users", userService.getUsers());
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
        modelAndView.addObject("invitations", userService.getInvitations());
        modelAndView.addObject("requestForm", new RequestForm());
        return modelAndView;
    }

    @PostMapping("/admin/invitations")
    public ModelAndView addInvitation(@ModelAttribute("requestForm") @Valid RequestForm requestForm, BindingResult result, Model model) {

            ModelAndView modelAndView;
            if (result.hasErrors()) {
                modelAndView = new ModelAndView("invitations");
                modelAndView.addObject("invitations",userService.getInvitations());
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
        modelAndView.addObject("invitations", userService.getInvitations());
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


}
