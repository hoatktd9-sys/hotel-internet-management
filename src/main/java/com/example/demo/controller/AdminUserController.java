package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository, RoleRepository roleRepository,
                               PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin/users/list";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String email,
                             @RequestParam(required = false) List<Long> roleIds,
                             RedirectAttributes redirectAttributes) {
        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username đã tồn tại");
            return "redirect:/admin/users";
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setActive(true);
        
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            user.setRoles(roles);
        }
        
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Tạo tài khoản nhân viên thành công");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/toggle-lock/{id}")
    public String toggleLock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setActive(!user.isActive());
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", user.isActive() ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/reset-password/{id}")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Khôi phục mật khẩu thành công");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/assign-role/{id}")
    public String assignRole(@PathVariable Long id, @RequestParam(required = false) List<Long> roleIds, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            if (roleIds != null && !roleIds.isEmpty()) {
                Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
                user.setRoles(roles);
            } else {
                user.setRoles(new HashSet<>());
            }
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Gán role thành công");
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/roles/update-permissions/{id}")
    public String updatePermissions(@PathVariable Long id, @RequestParam(required = false) List<Long> permissionIds, RedirectAttributes redirectAttributes) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role != null) {
            if (permissionIds != null && !permissionIds.isEmpty()) {
                role.setPermissions(new HashSet<>(permissionRepository.findAllById(permissionIds)));
            } else {
                role.setPermissions(new HashSet<>());
            }
            roleRepository.save(role);
            redirectAttributes.addFlashAttribute("success", "Cập nhật quyền thành công");
        }
        return "redirect:/admin/roles";
    }
}
