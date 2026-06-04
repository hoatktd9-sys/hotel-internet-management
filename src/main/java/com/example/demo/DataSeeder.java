package com.example.demo;

import com.example.demo.model.Permission;
import com.example.demo.model.CheckIn;
import com.example.demo.model.Bill;
import com.example.demo.repository.BillRepository;
import com.example.demo.repository.CheckInRepository;
import java.time.LocalDateTime;
import java.time.Duration;
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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final BillRepository billRepository;
    private final CheckInRepository checkInRepository;

    public DataSeeder(PermissionService permissionService,
                      RoleService roleService,
                      UserService userService,
                      org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                      BillRepository billRepository,
                      CheckInRepository checkInRepository) {
        this.permissionService = permissionService;
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.billRepository = billRepository;
        this.checkInRepository = checkInRepository;
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
                Map.entry("View_History", "Xem danh lịch sử check-in"),
                Map.entry("Admin_Service", "Quản lý danh mục và sản phẩm dịch vụ"),
                Map.entry("Order_Service", "Gọi món và sử dụng dịch vụ")
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
        java.util.Optional<User> adminOpt = userService.findByUsername(defaultAdminUsername);
        if (adminOpt.isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername(defaultAdminUsername);
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@gmail.com");
            adminUser.setActive(true);
            Set<Role> roles = new HashSet<>();
            roles.add(roleAdmin);
            adminUser.setRoles(roles);
            userService.save(adminUser);
            System.out.println(">>> Đã khởi tạo thành công tài khoản admin mặc định! <<<");
        } else {
            User adminUser = adminOpt.get();
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setActive(true);
            if (adminUser.getRoles() == null || adminUser.getRoles().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                roles.add(roleAdmin);
                adminUser.setRoles(roles);
            } else {
                boolean hasAdmin = false;
                for (Role r : adminUser.getRoles()) {
                    if ("ADMIN".equals(r.getRoleName())) {
                        hasAdmin = true;
                        break;
                    }
                }
                if (!hasAdmin) {
                    adminUser.getRoles().add(roleAdmin);
                }
            }
            userService.save(adminUser);
            System.out.println(">>> Đã cập nhật mật khẩu mã hóa và quyền ADMIN cho tài khoản admin! <<<");
        }
        
        repairExistingBills();
    }

    @Transactional
    public void repairExistingBills() {
        System.out.println(">>> Bắt đầu rà soát và phục hồi dữ liệu hóa đơn/thống kê bị thiếu... <<<");
        java.util.List<Bill> bills = billRepository.findAll();
        int repairedCount = 0;
        for (Bill bill : bills) {
            boolean checkInUpdated = false;
            CheckIn checkIn = bill.getCheckIn();
            if (checkIn != null) {
                // 1. Phục hồi các trường thông tin trên CheckIn
                LocalDateTime ciTime = checkIn.getCheckInTime();
                LocalDateTime coTime = checkIn.getCheckOutTime();
                if (coTime == null) {
                    coTime = bill.getPaymentTime() != null ? bill.getPaymentTime() : LocalDateTime.now();
                    checkIn.setCheckOutTime(coTime);
                    checkInUpdated = true;
                }
                
                double roomPrice = (checkIn.getRoom() != null) ? checkIn.getRoom().getPrice() : 0.0;
                double expected = checkIn.getExpectedHours() != null ? checkIn.getExpectedHours() : 0.0;
                
                double totalHours = checkIn.getTotalHours() != null ? checkIn.getTotalHours() : 0.0;
                if (totalHours == 0.0 && ciTime != null && coTime != null) {
                    totalHours = Math.max(1.0, Duration.between(ciTime, coTime).toMinutes() / 60.0);
                    checkIn.setTotalHours(totalHours);
                    checkInUpdated = true;
                }
                
                double roomTotal = (totalHours > expected && expected > 0.0) ? (expected * roomPrice) : (totalHours * roomPrice);
                double overtime = 0.0;
                if (checkIn.getOvertimeHours() == null || checkIn.getOvertimeHours() == 0.0) {
                    if (totalHours > expected && expected > 0.0) {
                        checkIn.setOvertimeHours(totalHours - expected);
                        overtime = (totalHours - expected) * roomPrice * 1.5;
                        checkIn.setOvertimeCharge(overtime);
                        checkInUpdated = true;
                    }
                } else {
                    overtime = checkIn.getOvertimeCharge() != null ? checkIn.getOvertimeCharge() : 0.0;
                }
                
                if (checkIn.getSurcharge() == null) {
                    checkIn.setSurcharge(0.0);
                    checkInUpdated = true;
                }
                
                if (checkInUpdated) {
                    checkInRepository.save(checkIn);
                }
                
                // 2. Phục hồi các trường thông tin thống kê trên Bill
                boolean billUpdated = false;
                if (bill.getRoomPriceReal() == null) {
                    bill.setRoomPriceReal(roomTotal);
                    billUpdated = true;
                }
                if (bill.getOvertimePriceReal() == null) {
                    bill.setOvertimePriceReal(overtime);
                    billUpdated = true;
                }
                if (bill.getSurchargeReal() == null) {
                    bill.setSurchargeReal(checkIn.getSurcharge() != null ? checkIn.getSurcharge() : 0.0);
                    billUpdated = true;
                }
                if (bill.getServicePriceReal() == null) {
                    double finalAmt = bill.getFinalAmount() != null ? bill.getFinalAmount() : 0.0;
                    double surchargeVal = bill.getSurchargeReal() != null ? bill.getSurchargeReal() : 0.0;
                    double servicePriceVal = finalAmt - roomTotal - overtime - surchargeVal;
                    if (servicePriceVal < 0.0) {
                        servicePriceVal = 0.0;
                    }
                    bill.setServicePriceReal(servicePriceVal);
                    billUpdated = true;
                }
                
                if (billUpdated) {
                    billRepository.save(bill);
                    repairedCount++;
                }
            }
        }
        System.out.println(">>> Đã hoàn tất sửa đổi/phục hồi " + repairedCount + " hóa đơn cũ thành công! <<<");
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
