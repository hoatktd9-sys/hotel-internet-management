package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.service.PermissionService;
import com.example.demo.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/roles")
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
    @GetMapping()
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.findAllRoles());
        model.addAttribute("allPermissions", permissionservice.findAll());
        return "/admin/roles/list";
    }
    @GetMapping("/create")
    public String createRole(Model model) {
        model.addAttribute("role", new Role());
        model.addAttribute("allPermissions", permissionservice.findAll());
        return "/admin/roles/create";
    }
    @PostMapping("/save")
    public String saveRole(@Valid @ModelAttribute("role")Role role, BindingResult result) {
       if(result.hasErrors()){
           return "/admin/roles/create";
       }
       else {roleService.saveRole(role);
           return "redirect:/admin/roles";
       }
    }
}
