package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.services.mvc.UsersModelController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
public class UsersController {


    private final UsersModelController facadeService;

    @Autowired
    UsersController(UsersModelController facadeService) {
        this.facadeService = facadeService;
    }


    @GetMapping("")
    public ModelAndView users() {
        return facadeService.getUsersPage(new ModelAndView("users"));
    }

    @DeleteMapping("/{id}")
    public RedirectView deleteUser(@PathVariable("id") UUID id) {
        facadeService.deleteUser(id);
        return new RedirectView("/admin/users");
    }

    @GetMapping("/{id}/role")
    public ModelAndView openRolesManager(@PathVariable("id") String id) {
        return facadeService.openRolesManager(new ModelAndView("/role"), id);
    }

    @PostMapping("/{id}/role")
    public RedirectView changeRole(@ModelAttribute("dto") ChangeRoleDto dto, @PathVariable("id") String id) {
        dto.setId(id);
        facadeService.changeRole(dto);
        return new RedirectView("/admin/users");
    }

    @PostMapping("/{id}/enabled/{status}")
    public RedirectView toggleUserEnabledStatus(@PathVariable("id") String id, @PathVariable("status") boolean status) {
        facadeService.toggleUserEnabledStatus(id, status);
        return new RedirectView("/admin/users");
    }
}
