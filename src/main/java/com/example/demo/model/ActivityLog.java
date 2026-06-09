package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // Người thực hiện (Lấy từ Spring Security)

    private String action; // Ví dụ: CREATE_ROOM, UPDATE_ROOM, CHECKIN, CHECKOUT, DELETE_ROOM

    @Column(columnDefinition = "TEXT")
    private String detail; // Chi tiết thao tác (Ví dụ: "Đã check-in phòng P101 cho khách Nguyễn Văn A")

    private LocalDateTime timestamp;

    // ===== CONSTRUCTOR =====
    public ActivityLog() {
    }

    public ActivityLog(String username, String action, String detail) {
        this.username = username;
        this.action = action;
        this.detail = detail;
        this.timestamp = LocalDateTime.now();
    }

    // ===== GETTER / SETTER =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}