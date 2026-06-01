package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.LoginHistoryRepository;
import com.example.demo.service.UserService;
import org.springframework.data.domain.Page;
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
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginHistoryRepository loginHistoryRepository;

    public AdminUserController(UserService userService, RoleRepository roleRepository,
                               PermissionRepository permissionRepository, PasswordEncoder passwordEncoder,
                               LoginHistoryRepository loginHistoryRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginHistoryRepository = loginHistoryRepository;
    }


    @PostMapping("/users/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String email,
                             @RequestParam(required = false) List<Long> roleIds,
                             RedirectAttributes redirectAttributes) {
        if (userService.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Username đã tồn tại");
            return "redirect:/admin/users";
        }
        if (userService.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại");
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

        userService.save(user);
        redirectAttributes.addFlashAttribute("success", "Tạo tài khoản nhân viên thành công");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/toggle-lock/{id}")
    public String toggleLock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id);
        if (user != null) {
            user.setActive(!user.isActive());
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", user.isActive() ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản");
            return "redirect:/admin/users";
        }
        redirectAttributes.addFlashAttribute("error", "Khóa thất bại");
        return "redirect:/admin/users";

    }

    @PostMapping("/users/reset-password/{id}")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Khôi phục mật khẩu thành công");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/assign-role/{id}")
    public String assignRole(@PathVariable Long id, @RequestParam(required = false) List<Long> roleIds, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id);
        if (user != null) {
            if (roleIds != null && !roleIds.isEmpty()) {
                Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
                user.setRoles(roles);
            } else {
                user.setRoles(new HashSet<>());
            }
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Gán role thành công");
            return "redirect:/admin/users";
        }
        redirectAttributes.addFlashAttribute("error", "Gán role thất bại ");
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

    @GetMapping("/users/delete/{id}")
    String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa Thất Bại");
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String newUsername,
                             @RequestParam String newEmail,
                             RedirectAttributes redirectAttributes) {

        User user = userService.findById(id);
        if (user != null) {
            user.setUsername(newUsername);
            user.setEmail(newEmail);
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công");
            return "redirect:/admin/users";
        }
        redirectAttributes.addFlashAttribute("error", "Chỉnh sửa thất bại (Không tìm thấy thành viên)");
        return "redirect:/admin/users";
    }

    @GetMapping("/users")
    public String listUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        String roleName = null;
        if (roleId != null) {
            java.util.Optional<com.example.demo.model.Role> optionalRole = roleRepository.findById(roleId);
            roleName = optionalRole.map(com.example.demo.model.Role::getRoleName).orElse(null);
        }
        
        Page<User> userPage = userService.searchUser(username, email, roleName, page, size);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("allRoles", roleRepository.findAll());
        model.addAttribute("usernameKey", username);
        model.addAttribute("emailKey", email);
        model.addAttribute("roleKey", roleName);
        model.addAttribute("roleIdKey", roleId);

        return "admin/users/list";
    }

    @GetMapping("/login-history")
    public String loginHistory(Model model) {
        model.addAttribute("history", loginHistoryRepository.findAllByOrderByLoginTimeDesc());
        return "admin/users/login_history";
    }
}
