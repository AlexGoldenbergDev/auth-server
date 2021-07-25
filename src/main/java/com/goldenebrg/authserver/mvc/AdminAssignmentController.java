package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.security.auth.service.UserDetailsImpl;
import com.goldenebrg.authserver.services.AssignmentsService;
import com.goldenebrg.authserver.services.UserService;
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/assignments")
public class AdminAssignmentController {


    private final AssignmentsService assignmentsService;
    private final UserService userService;


    @Autowired
    AdminAssignmentController(AssignmentsService assignmentsService, UserService userService) {
        this.assignmentsService = assignmentsService;
        this.userService = userService;
    }

    public static <T extends AbstractAssignmentField> Map<String, T> filterFieldsByRole(Map<String, T> map, String role) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().getChangers().contains(role))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @DeleteMapping("/{id}")
    public RedirectView assignments(@PathVariable("id") UUID id) {
        assignmentsService.deleteById(id);
        return new RedirectView("/admin/assignments");
    }

    @GetMapping("")
    public ModelAndView assignmentsMain() {
        ModelAndView modelAndView = new ModelAndView("assignments");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        @NonNull String role = userDetails.getUser().getRole();

        modelAndView.addObject("assignmentsMap", assignmentsService.getUsersAssignmentsMap());
        modelAndView.addObject("assignments", assignmentsService.getAllAssignmentsNames(role));
        return modelAndView;
    }

    @GetMapping("/master/{assignment}/{user}")
    public ModelAndView assignmentsPost(@PathVariable("assignment") String assignment, @PathVariable("user") String user) {

        ModelAndView modelAndView = new ModelAndView("assignments_master2");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        @NonNull String role = userDetails.getUser().getRole();

        Map<String, AssignmentSelectionListField> selectionListFieldsMap = filterFieldsByRole(assignmentsService.getAssignmentSelectionListFieldsMap(assignment), role);
        Map<String, AssignmentInputField> inputFieldsMap = filterFieldsByRole(assignmentsService.getAssignmentInputFieldsMap(assignment), role);

        modelAndView.addObject("selectionListFieldsMap", selectionListFieldsMap);
        modelAndView.addObject("inputFieldsMap", inputFieldsMap);

        Optional.ofNullable(userService.getUserById(UUID.fromString(user)))
                .ifPresent(userObj -> {
                    Map<String, Serializable> fields = Optional.ofNullable(userObj.getUserServices())
                            .map(map -> map.get(assignment))
                            .map(UserAssignments::getFields)
                            .orElse(new HashMap<>());


                    modelAndView.addObject("persistedField", fields);
                });


        modelAndView.addObject("dto", new AssignmentForm());
        modelAndView.addObject("assignment", assignment);
        modelAndView.addObject("user", user);


        return modelAndView;
    }

    @PostMapping("/master/{assignment}/{user}")
    public RedirectView assignmentsPost(@PathVariable("assignment") String assignment, @PathVariable("user") String user, @ModelAttribute("dto") AssignmentForm dto) {
        dto.setAssignment(assignment);
        assignmentsService.save(user, dto);
        return new RedirectView("/admin/assignments");

    }
}
