package com.example.demo;

import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.PermissionService;
import com.example.demo.service.RoleService;
import com.example.demo.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {
    private final PermissionService permissionService;
    private final RoleService roleService;
    private final UserService userService;

    public DataSeeder(PermissionService permissionService, RoleService roleService, UserService userService) {
        this.permissionService = permissionService;
        this.roleService = roleService;
        this.userService = userService;
    }
    @Transactional
    public void run(String... args) throws Exception {
        Map<String, String> permissions = Map.ofEntries(
                Map.entry("View_Customer", "Xem danh sách khách hàng"),
                Map.entry("Edit_Customer", "Chỉnh sửa khách hàng"),
                Map.entry("Delete_Customer", "Xóa khách hàng"),
                Map.entry("Create_Customer", "Tạo mới khách hàng"),
                Map.entry("Create_Room", "Tạo mới phòng"),
                Map.entry("Edit_Room", "Chỉnh sửa phòng"),
                Map.entry("Delete_Room", "Xóa phòng"),
                Map.entry("View_Room", "Xem danh sách phòng"),
                Map.entry("View_History", "Xem danh lịch sử check-in")
        );
        Set<Permission> AdminPermissions = new HashSet<>();
        for(Map.Entry<String,String> entry : permissions.entrySet()){
            Permission p =   createPermissionIfNotFound(entry.getKey(), entry.getValue());
            AdminPermissions.add(p);
        };
        Role roleAdmin = roleService.findByRoleName("ADMIN");
        if(roleAdmin==null){
            roleAdmin = new Role();
            roleAdmin.setRoleName("ADMIN");
            roleAdmin.setDescription("Bạn Là Quản Trị Viên");
        }
        roleAdmin.setPermissions(AdminPermissions);
        roleService.saveRole(roleAdmin);

        String defaultAdminUsername = "admin";
        if (userService.findByUsername(defaultAdminUsername).isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername(defaultAdminUsername);
            adminUser.setPassword("admin123");
            adminUser.setEmail("admin@gmail.com");
            adminUser.setActive(true);
            Set<Role> roles = new HashSet<>();
            roles.add(roleAdmin);
            adminUser.setRoles(roles);
            userService.save(adminUser);
            System.out.println(">>> Đã khởi tạo thành công tài khoản admin mặc định! <<<");
        }
    }



    public Permission createPermissionIfNotFound(String permissionName, String description) {
        return permissionService.findByName(permissionName).orElseGet(() -> {
            Permission permission = new Permission();
            permission.setPermissionName(permissionName);
            permission.setDescription(description);
            return permissionService.save(permission);
        });
    }
}
