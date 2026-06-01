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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/roles")
public class RoleController {

   private RoleService roleService;

   private PermissionService permissionservice;

    public RoleController(PermissionService permissionservice, RoleService roleService) {
        this.permissionservice = permissionservice;
        this.roleService = roleService;
    }

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
    @GetMapping("/delete/{id}")
    public String deleteRole(@PathVariable long id, RedirectAttributes redirectAttributes){
            try{
                roleService.deleteRoleById(id);
                redirectAttributes.addFlashAttribute("success","Xóa thành công");
                return "redirect:/admin/roles";
            }
            catch (Exception e) {
                redirectAttributes.addFlashAttribute("error","Xóa thất bại");
                return "redirect:/admin/roles";
            }
    }
}
