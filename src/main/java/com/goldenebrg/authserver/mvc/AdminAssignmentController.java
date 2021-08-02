package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.security.auth.service.UserDetailsImpl;
import com.goldenebrg.authserver.services.FacadeService;
import com.goldenebrg.authserver.services.config.AbstractAssignmentField;
import com.goldenebrg.authserver.services.config.AssignmentInputField;
import com.goldenebrg.authserver.services.config.AssignmentSelectionListField;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/assignments")
public class AdminAssignmentController {


    private final FacadeService facadeService;


    @Autowired
    AdminAssignmentController(FacadeService facadeService) {
        this.facadeService = facadeService;
    }

    public static <T extends AbstractAssignmentField> Map<String, T> filterFieldsByRole(Map<String, T> map, String role) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().getChangers().contains(role))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @DeleteMapping("/{id}")
    public RedirectView assignments(@PathVariable("id") UUID id) {
        facadeService.deleteService(id);
        return new RedirectView("/admin/assignments");
    }

    @GetMapping("")
    public ModelAndView assignmentsMain() {
        ModelAndView modelAndView = new ModelAndView("assignments");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        @NonNull String role = userDetails.getUser().getRole();

        modelAndView.addObject("assignmentsMap", facadeService.getAdminAssignmentsMap());
        modelAndView.addObject("assignments", facadeService.getAssignmentsNames(role));
        return modelAndView;
    }

    @GetMapping("/master/{assignment}/{user}")
    public ModelAndView assignmentsPost(@PathVariable("assignment") String assignment, @PathVariable("user") String user) {

        ModelAndView modelAndView = new ModelAndView("assignments_master2");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> targetUser = facadeService.findUser(user);


        @NonNull String role = userDetails.getUser().getRole();

        Map<String, AssignmentSelectionListField> selectionListFieldsMap = filterFieldsByRole(facadeService.getAssignmentSelectionListFieldsMap(assignment), role);
        Map<String, AssignmentInputField> inputFieldsMap = filterFieldsByRole(facadeService.getAssignmentInputFieldsMap(assignment), role);

        modelAndView.addObject("selectionListFieldsMap", selectionListFieldsMap);
        modelAndView.addObject("inputFieldsMap", inputFieldsMap);

        Map<String, String> fields = targetUser.map(User::getUserServices)
                .map(map -> map.get(assignment))
                .map(UserAssignments::getFields).map(f -> f.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue()))))
                .orElse(Collections.emptyMap());


        AssignmentForm assignmentForm = new AssignmentForm();
        assignmentForm.setFields(fields);
        assignmentForm.setAssignment(assignment);

        modelAndView.addObject("dto", assignmentForm);
        modelAndView.addObject("assignment", assignment);
        modelAndView.addObject("user", user);


        return modelAndView;
    }

    @PostMapping("/master/{user}")
    public RedirectView assignmentsPost(@PathVariable("user") String user, @ModelAttribute("dto") AssignmentForm dto) {
        facadeService.createService(user, dto);
        return new RedirectView("/admin/assignments");

    }
}
