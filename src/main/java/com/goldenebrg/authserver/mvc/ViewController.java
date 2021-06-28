package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.rest.beans.LoginDto;
import com.goldenebrg.authserver.rest.beans.UserDto;
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
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({ "/index", "/" })
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView("index");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
            @NonNull UUID id = principal.getUser().getId();
            modelAndView.addObject("user", userService.getUserById(id));
        }
        else {
            modelAndView.addObject("login", new LoginDto());

        }


        return modelAndView;
    }

   /* @GetMapping({ "/user" })
    public ModelAndView user() {
        ModelAndView modelAndView = new ModelAndView("user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        @NonNull UUID id = principal.getUser().getId();
        modelAndView.addObject("user", userService.getUserById(id));
        return modelAndView;
    }*/

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
    public String registerUserAccount
            (@ModelAttribute("user")  UserDto userDto,
             @ModelAttribute("uuid")  String uuid,
             HttpServletRequest request, Errors errors) {

        try {
            UUID id = UUID.fromString(uuid);
            userDto.setUuid(id);
            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(userDto.getLogin(), userDto.getPassword());
            userService.registerNewUserAccount(userDto);
            userService.deleteRequestById(id);

            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authenticationToken);
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
        } catch (Exception uaeEx) {

        }

        return "index";
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
        return modelAndView;
    }

    @PostMapping("/admin/invitations")
    public RedirectView addInvitation(@ModelAttribute("email") String email) {
        userService.createRequest(email);
        return new RedirectView("/admin/invitations");
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
