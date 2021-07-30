package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.rest.beans.LoginDto;
import com.goldenebrg.authserver.security.auth.service.UserDetailsImpl;
import com.goldenebrg.authserver.services.AssignmentsService;
import com.goldenebrg.authserver.services.UserService;
import com.goldenebrg.authserver.services.config.AssignmentInputField;
import com.goldenebrg.authserver.services.config.AssignmentSelectionListField;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.Serializable;
import java.util.*;

import static com.goldenebrg.authserver.mvc.AdminAssignmentController.filterFieldsByRole;

@Controller
@Slf4j
public class RootController {

    private final UserService userService;
    private final AssignmentsService assignmentsService;


    @Autowired
    RootController(UserService userService, AssignmentsService assignmentsService) {
        this.userService = userService;
        this.assignmentsService = assignmentsService;
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
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                User u = user.get();
                Map<String, Set<String>> assignments = assignmentsService.getAssignmentPrints(u);

                Set<String> available = assignmentsService.getAllAssignmentsNames(u.getRole());
                available.removeAll(u.getUserServices().keySet());

                modelAndView.addObject("user", user);
                modelAndView.addObject("assignments", assignments);
                modelAndView.addObject("assignmentToAdd", new AssignmentForm());
                modelAndView.addObject("availableServices", available);
            } else
                modelAndView.addObject("login", new LoginDto());
        } else
            modelAndView.addObject("login", new LoginDto());


        return modelAndView;
    }


    @PostMapping("/assignments/edit")
    public ModelAndView assignmentsPost(@ModelAttribute("assignmentToAdd") AssignmentForm assignmentToAdd) {

        ModelAndView modelAndView = new ModelAndView("assignments_master");
        String assignment = assignmentToAdd.getAssignment();
        return assignmentsPost(modelAndView, assignment);
    }


    @PostMapping("/assignments/edit/{service}")
    public ModelAndView assignmentsEdit(@PathVariable("service") String service) {

        ModelAndView modelAndView = new ModelAndView("assignments_master");
        return assignmentsPost(modelAndView, service);
    }


    public ModelAndView assignmentsPost(ModelAndView modelAndView, String assignment) {


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        @NonNull UUID id = userDetails.getUser().getId();
        Optional<User> userObj = userService.getUserById(id);


        @NonNull String role = userDetails.getUser().getRole();


        Map<String, AssignmentSelectionListField> selectionListFieldsMap = filterFieldsByRole(assignmentsService.getAssignmentSelectionListFieldsMap(assignment), role);
        Map<String, AssignmentInputField> inputFieldsMap = filterFieldsByRole(assignmentsService.getAssignmentInputFieldsMap(assignment), role);

        modelAndView.addObject("selectionListFieldsMap", selectionListFieldsMap);
        modelAndView.addObject("inputFieldsMap", inputFieldsMap);

        Map<String, Serializable> fields = userObj.map(User::getUserServices)
                .map(map -> map.get(assignment))
                .map(UserAssignments::getFields)
                .orElse(new HashMap<>());


        modelAndView.addObject("persistedField", fields);
        modelAndView.addObject("dto", new AssignmentForm());
        modelAndView.addObject("assignment", assignment);
        modelAndView.addObject("user", id.toString());


        return modelAndView;
    }


    @PostMapping("/assignments/{service}/{user}")
    public RedirectView assignmentsPost(@PathVariable("service") String assignment, @PathVariable("user") String user, @ModelAttribute("dto") AssignmentForm dto) {
        dto.setAssignment(assignment);
        assignmentsService.save(user, dto);
        return new RedirectView("/");

    }

    @DeleteMapping("/assignments/delete/{service}")
    public RedirectView assignmentsPost(@PathVariable("service") String service) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        assignmentsService.deleteService(userDetails.getUsername(), service);
        return new RedirectView("/");

    }


    @GetMapping("/login")
    public ModelAndView failedLogin(@RequestParam(name = "message") String message) {
        ModelAndView modelAndView = index();
        modelAndView.addObject("message", message);
        return modelAndView;
    }


}
