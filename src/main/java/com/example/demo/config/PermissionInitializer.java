package com.example.demo.config;

import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Configuration
public class PermissionInitializer {

    @Bean
    public CommandLineRunner initPermissions(PermissionRepository repository, RoleRepository roleRepository) {
        return args -> {
            // Sử dụng Map để định nghĩa cặp: "Tên quyền" -> "Mô tả tiếng Việt chuẩn UX"
            Map<String, String> systemPermissions = new LinkedHashMap<>();

            // 1. Các quyền cũ của hệ thống
            systemPermissions.put("Delete_Room", "Xóa phòng");
            systemPermissions.put("Create_Customer", "Tạo mới khách hàng");
            systemPermissions.put("Create_Room", "Tạo mới phòng");
            systemPermissions.put("Delete_Customer", "Xóa khách hàng");
            systemPermissions.put("View_Customer", "Xem danh sách khách hàng");
            systemPermissions.put("Edit_Customer", "Chỉnh sửa khách hàng");
            systemPermissions.put("View_History", "Xem danh lịch sử check-in");
            systemPermissions.put("View_Room", "Xem danh sách phòng");
            systemPermissions.put("Edit_Room", "Chỉnh sửa phòng");

            // 2. Cấu hình mô tả chuẩn cho các quyền mới thuộc Epic Dịch vụ
            systemPermissions.put("Admin_Service", "Quản lý danh mục và sản phẩm dịch vụ");
            systemPermissions.put("Order_Service", "Gọi món và sử dụng dịch vụ");

            // Vòng lặp quét tự động để đồng bộ Permission dưới Database
            for (Map.Entry<String, String> entry : systemPermissions.entrySet()) {
                String pName = entry.getKey();
                String pDescription = entry.getValue();

                Optional<Permission> existingPermission = repository.findByPermissionName(pName);

                if (!existingPermission.isPresent()) {
                    Permission p = new Permission();
                    p.setPermissionName(pName);
                    p.setDescription(pDescription);

                    repository.save(p);
                    System.out.println("=== [AUTO INIT] Đã thêm mới quyền: " + pName + " (" + pDescription + ")");
                } else {
                    Permission p = existingPermission.get();
                    if (p.getDescription() == null || !p.getDescription().equals(pDescription)) {
                        p.setDescription(pDescription);
                        repository.save(p);
                        System.out.println("=== [AUTO INIT] Đã cập nhật mô tả mới thành công cho quyền: " + pName);
                    }
                }
            }

            // =========================================================================
            // CƠ CHẾ TỰ ĐỘNG BÙ QUYỀN CHO ADMIN - DÙNG HÀM findByRoleName SẴN CÓ
            // =========================================================================
            Role adminRole = roleRepository.findByRoleName("ADMIN");

            if (adminRole != null) {
                Set<Permission> currentAdminPermissions = adminRole.getPermissions();
                boolean isUpdated = false;

                // Danh sách các quyền mới cần gán cứng cho ADMIN khi khởi động
                String[] servicePermissions = {"Admin_Service", "Order_Service"};

                for (String pName : servicePermissions) {
                    Optional<Permission> permOpt = repository.findByPermissionName(pName);
                    if (permOpt.isPresent()) {
                        Permission targetPerm = permOpt.get();

                        // Kiểm tra xem ADMIN hiện tại đã có quyền này chưa
                        boolean alreadyHas = currentAdminPermissions.stream()
                                .anyMatch(p -> p.getPermissionName().equals(targetPerm.getPermissionName()));

                        if (!alreadyHas) {
                            currentAdminPermissions.add(targetPerm); // Thêm quyền mới vào danh sách của ADMIN
                            isUpdated = true;
                            System.out.println("=== [AUTO CẤP QUYỀN] Đã chủ động gán thêm quyền '" + pName + "' cho vai trò ADMIN.");
                        }
                    }
                }

                // Nếu có cập nhật quyền mới thì lưu lại ngay vào Database
                if (isUpdated) {
                    adminRole.setPermissions(currentAdminPermissions);
                    roleRepository.save(adminRole);
                }
            }
        };
    }
}