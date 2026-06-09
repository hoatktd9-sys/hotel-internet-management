package com.example.demo.controller;

import com.example.demo.service.DatabaseBackupService;
import com.example.demo.service.ActivityLogService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/admin/backup")
public class BackupController {

    private final DatabaseBackupService backupService;
    private final ActivityLogService activityLogService;

    public BackupController(DatabaseBackupService backupService, ActivityLogService activityLogService) {
        this.backupService = backupService;
        this.activityLogService = activityLogService;
    }

    /**
     * Endpoint tải file sao lưu Database (.sql) về máy
     */
    @GetMapping("/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadBackup() {
        try {
            // Gọi service sinh mảng byte của file SQL
            byte[] sqlData = backupService.exportDatabaseToSql();

            // Đặt tên file theo định dạng: gaming_hotel_backup_yyyyMMdd_HHmmss.sql
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "gaming_hotel_backup_" + timestamp + ".sql";

            // Thiết lập các Header để trình duyệt hiểu là cần tải file về (Download)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);

            // [LOG CHÈN THÊM]: Lưu lại nhật ký hành động thao tác hệ thống
            activityLogService.log("DATABASE_BACKUP", "Admin thực hiện sao lưu dữ liệu hệ thống. Tên file: " + fileName);

            return new ResponseEntity<>(sqlData, headers, HttpStatus.OK);

        } catch (IOException | InterruptedException | RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint nhận file .sql từ giao diện để thực hiện khôi phục dữ liệu
     * Tự động quay về trang cũ nhờ vào Referer Header
     */
    @PostMapping("/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public String handleRestoreDatabase(@RequestParam("file") MultipartFile file,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {

        // Lấy URL của trang Admin đang đứng trước khi bấm khôi phục để tí nữa redirect về đúng chỗ đó
        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null) ? "redirect:" + referer : "redirect:/admin/reports/dashboard";

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn một file .sql hợp lệ để khôi phục!");
            return redirectUrl;
        }

        try {
            // Gọi service thực hiện khôi phục dữ liệu bằng mảng byte
            backupService.restoreDatabaseFromSql(file.getBytes());

            // [LOG CHÈN THÊM]: Lưu lại nhật ký hành động phục hồi hệ thống
            activityLogService.log("DATABASE_RESTORE", "Admin thực hiện khôi phục dữ liệu từ file: " + file.getOriginalFilename());

            redirectAttributes.addFlashAttribute("success", "Khôi phục dữ liệu thành công! Hệ thống đã quay về trạng thái cũ.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Quá trình khôi phục thất bại: " + e.getMessage());
        }

        return redirectUrl; // Điều hướng quay trở lại trang cũ một cách an toàn
    }
}