package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;

@Service
public class DatabaseBackupService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password:#{null}}")
    private String dbPassword;

    private String getDatabaseName() {
        try {
            String cleanUrl = dbUrl.replace("jdbc:mysql://", "");
            String dbName = cleanUrl.substring(cleanUrl.indexOf("/") + 1);
            if (dbName.contains("?")) {
                dbName = dbName.substring(0, dbName.indexOf("?"));
            }
            return dbName;
        } catch (Exception e) {
            throw new RuntimeException("Không thể phân tích tên Database từ URL.");
        }
    }

    // --- LOGIC SAO LƯU (BACKUP) AN TOÀN ---
    public byte[] exportDatabaseToSql() throws IOException, InterruptedException {
        String dbName = getDatabaseName();
        String mysqldumpPath = "mysqldump"; // Mặc định nếu đã cài biến môi trường

        // Danh sách các đường dẫn cài đặt MySQL phổ biến trên Windows
        String[] commonPaths = {
                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
                "C:\\xampp\\mysql\\bin\\mysqldump.exe"
        };
        for (String path : commonPaths) {
            if (new File(path).exists()) {
                mysqldumpPath = path;
                break;
            }
        }

        ProcessBuilder pb;
        if (dbPassword != null && !dbPassword.trim().isEmpty()) {
            pb = new ProcessBuilder(mysqldumpPath, "-u" + dbUser, "-p" + dbPassword, dbName);
        } else {
            pb = new ProcessBuilder(mysqldumpPath, "-u" + dbUser, dbName);
        }

        Process process = pb.start();

        try (InputStream is = process.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Lỗi thực thi mysqldump. Mã thoát: " + exitCode);
            }
            return bos.toByteArray();
        }
    }

    // --- LOGIC KHÔI PHỤC (RESTORE) AN TOÀN ---
    public void restoreDatabaseFromSql(byte[] fileBytes) throws IOException, InterruptedException {
        String dbName = getDatabaseName();
        String mysqlPath = "mysql";

        String[] commonPaths = {
                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysql.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
                "C:\\xampp\\mysql\\bin\\mysql.exe"
        };
        for (String path : commonPaths) {
            if (new File(path).exists()) {
                mysqlPath = path;
                break;
            }
        }

        ProcessBuilder pb;
        if (dbPassword != null && !dbPassword.trim().isEmpty()) {
            pb = new ProcessBuilder(mysqlPath, "-u" + dbUser, "-p" + dbPassword, dbName);
        } else {
            pb = new ProcessBuilder(mysqlPath, "-u" + dbUser, dbName);
        }

        Process process = pb.start();

        try (OutputStream os = process.getOutputStream()) {
            os.write(fileBytes);
            os.flush();
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Lỗi thực thi mysql restore. Mã thoát: " + exitCode);
        }
    }
}