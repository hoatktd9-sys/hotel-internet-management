package com.example.demo.controller;

import com.example.demo.repository.PermissionRepository;
import com.example.demo.service.PermissionService;
import com.example.demo.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class RoleController {
    @Autowired
   private RoleService roleService;
    @Autowired
   private PermissionService permissionservice;

   public RoleService getRoleService() {
        return roleService;
    }
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }
    @GetMapping("/roles")
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.findAllRoles());
        model.addAttribute("allPermissions", permissionservice.findAll());
        return "/admin/roles/list";
    }
}
