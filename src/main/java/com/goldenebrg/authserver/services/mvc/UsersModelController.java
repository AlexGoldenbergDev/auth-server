package com.goldenebrg.authserver.services.mvc;

import com.goldenebrg.authserver.aspects.UserAccess;
import com.goldenebrg.authserver.rest.beans.ChangeRoleDto;
import com.goldenebrg.authserver.services.FacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Service
public class UsersModelController {

    FacadeService facadeService;

    @Autowired
    UsersModelController(FacadeService facadeService) {
        this.facadeService = facadeService;
    }


    @UserAccess
    public ModelAndView getUsersPage(@NotNull ModelAndView modelAndView) {
        modelAndView.addObject("users", facadeService.getAllUsers());
        return modelAndView;
    }

    @UserAccess
    public void deleteUser(@NotNull UUID id) {
        facadeService.deleteUser(id);
    }

    @UserAccess
    public ModelAndView openRolesManager(@NotNull ModelAndView modelAndView, @NotNull @NotEmpty String id) {
        modelAndView.addObject("id", id);
        modelAndView.addObject("roles", facadeService.getAvailableRoles());
        modelAndView.addObject("dto", new ChangeRoleDto());
        return modelAndView;
    }

    @UserAccess
    public void changeRole(@NotNull ChangeRoleDto dto) {
        facadeService.changeRole(dto);
    }

    @UserAccess
    public void toggleUserEnabledStatus(@NotNull @NotEmpty String id, boolean status) {
        facadeService.changeEnabledStatus(id, status);
    }

}
