package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.AssignmentForm;
import com.goldenebrg.authserver.services.AssignmentsService;
import com.goldenebrg.authserver.services.config.AssignmentField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/assignments")
public class AssignmentController {


    private final AssignmentsService assignmentsService;


    @Autowired
    AssignmentController(AssignmentsService assignmentsService) {
        this.assignmentsService = assignmentsService;
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

    @PostMapping("/master/{assignment}/{user}")
    public RedirectView assignmentsPost(@PathVariable("assignment") String assignment, @PathVariable("user") String user, @ModelAttribute("dto") AssignmentForm dto) {
        dto.setAssignment(assignment);
        assignmentsService.save(user, dto);
        return new RedirectView("/admin/assignments");

    }
}
