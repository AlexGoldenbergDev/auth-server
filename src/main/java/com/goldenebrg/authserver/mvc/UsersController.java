package com.goldenebrg.authserver.mvc;

import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
public class UsersController {

    private final UserService userService;


    @Autowired
    UsersController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("")
    public ModelAndView users() {
        ModelAndView modelAndView = new ModelAndView("users");
        modelAndView.addObject("users", userService.getSortedUsers());
        return modelAndView;
    }

    @DeleteMapping("/{id}")
    public RedirectView deleteUser(@PathVariable("id") UUID id) {
        userService.deleteUserById(id);
        return new RedirectView("/admin/users");
    }

    @GetMapping("/{id}/role")
    public ModelAndView openRolesManager(@PathVariable("id") String id) {

        ModelAndView modelAndView = new ModelAndView("/role");
        modelAndView.addObject("id", id);
        modelAndView.addObject("roles", userService.getAvailableRoles());
        modelAndView.addObject("dto", new ChangeRoleDto());
        return modelAndView;
    }

    @PostMapping("/{id}/role")
    public RedirectView changeRole(@ModelAttribute("dto") ChangeRoleDto dto, @PathVariable("id") String id) {
        dto.setId(id);
        userService.changeRole(dto);
        return new RedirectView("/admin/users");
    }

    @PostMapping("/{id}/enabled/{status}")
    public RedirectView toggleUserEnabledStatus(@PathVariable("id") String id, @PathVariable("status") boolean status) {
        userService.toggleEnabledStatus(id, status);
        return new RedirectView("/admin/users");
    }
}
