package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.jpa.entities.User;
import com.goldenebrg.authserver.jpa.entities.UserAssignments;
import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.services.AssignmentsService;
import com.goldenebrg.authserver.services.UserService;
import com.goldenebrg.authserver.services.config.AssignmentInputField;
import com.goldenebrg.authserver.services.config.AssignmentSelectionListField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/assignments")
public class AssignmentController {


    private final AssignmentsService assignmentsService;
    private final UserService userService;


    @Autowired
    AssignmentController(AssignmentsService assignmentsService, UserService userService) {
        this.assignmentsService = assignmentsService;
        this.userService = userService;
    }

    @GetMapping("")
    public ModelAndView assignmentsMain() {
        ModelAndView modelAndView = new ModelAndView("assignments");
        modelAndView.addObject("assignmentsMap", assignmentsService.getUsersAssignmentsMap());
        modelAndView.addObject("assignments", assignmentsService.getAllAssignmentsNames());
        return modelAndView;
    }

    @DeleteMapping("/{id}")
    public RedirectView assignments(@PathVariable("id") UUID id) {
        assignmentsService.deleteById(id);
        return new RedirectView("/admin/assignments");
    }

    @GetMapping("/master/{assignment}/{user}")
    public ModelAndView assignmentsPost(@PathVariable("assignment") String assignment, @PathVariable("user") String user) {
        ModelAndView modelAndView = new ModelAndView();

        Map<String, AssignmentSelectionListField> selectionListFieldsMap = assignmentsService.getAssignmentSelectionListFieldsMap(assignment);
        Map<String, AssignmentInputField> inputFieldsMap = assignmentsService.getAssignmentInputFieldsMap(assignment);
        if (!selectionListFieldsMap.isEmpty() || !inputFieldsMap.isEmpty()) {

            Map<String, Serializable> fields = Optional.ofNullable(userService.getUserById(UUID.fromString(user))).map(User::getUserServices)
                    .map(map -> map.get(assignment)).map(UserAssignments::getFields).orElse(new HashMap<>());

            modelAndView = new ModelAndView("assignments_master2");
            modelAndView.addObject("dto", new AssignmentForm());
            modelAndView.addObject("selectionListFieldsMap", selectionListFieldsMap);
            modelAndView.addObject("inputFieldsMap", inputFieldsMap);
            modelAndView.addObject("assignment", assignment);
            modelAndView.addObject("persistedField", fields);
            modelAndView.addObject("user", user);
        }

        return modelAndView;
    }

    @PostMapping("/master/{assignment}/{user}")
    public RedirectView assignmentsPost(@PathVariable("assignment") String assignment, @PathVariable("user") String user, @ModelAttribute("dto") AssignmentForm dto) {
        dto.setAssignment(assignment);
        assignmentsService.save(user, dto);
        return new RedirectView("/admin/assignments");

    }
}
