package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "CCCD không được để trống")
    @Column(unique = true)
    private String identityNumber;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    // ĐÃ THÊM: Quản lý trạng thái VIP (mặc định ban đầu là false)
    private boolean isVip = false;

    // ĐÃ THÊM: Trạng thái tài khoản hoạt động hoặc bị khóa (mặc định ban đầu là true - hoạt động)
    private boolean isActive = true;

    // ===== Getter Setter =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}