package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkin_room")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== CUSTOMER =====
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // ===== ROOM =====
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    // ===== CHECK-IN =====
    private LocalDateTime checkInTime;

    // ===== CHECK-OUT =====
    private LocalDateTime checkOutTime;

    // ===== TOTAL HOURS =====
    private Double totalHours;

    // ===== TOTAL PRICE =====
    private Double totalPrice;

    // ===== RENTAL VOUCHER STATUS & EXTRA INFO =====
    private String status = "ACTIVE"; // ACTIVE, RESERVED, COMPLETED, CANCELLED

    private Double expectedHours = 0.0;

    private Double overtimeHours = 0.0;

    private Double overtimeCharge = 0.0;

    private Double surcharge = 0.0;

    // ===== GETTER SETTER =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public Double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Double totalHours) {
        this.totalHours = totalHours;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getExpectedHours() {
        return expectedHours;
    }

    public void setExpectedHours(Double expectedHours) {
        this.expectedHours = expectedHours;
    }

    public Double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(Double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public Double getOvertimeCharge() {
        return overtimeCharge;
    }

    public void setOvertimeCharge(Double overtimeCharge) {
        this.overtimeCharge = overtimeCharge;
    }

    public Double getSurcharge() {
        return surcharge;
    }

    public void setSurcharge(Double surcharge) {
        this.surcharge = surcharge;
    }

    // ===== KIỂM TRA PHÒNG SẮP HẾT GIỜ (DƯỚI 15 PHÚT) =====
    @Transient
    public boolean isAlmostOvertime() {
        if (!"ACTIVE".equals(this.status) || this.checkInTime == null || this.expectedHours == null || this.expectedHours <= 0) {
            return false;
        }
        // Tính toán thời điểm phải trả máy dự kiến
        // Số phút đã trôi qua kể từ lúc check-in đến hiện tại
        long minutesPassed = java.time.Duration.between(this.checkInTime, java.time.LocalDateTime.now()).toMinutes();
        long totalExpectedMinutes = (long) (this.expectedHours * 60);
        long minutesRemaining = totalExpectedMinutes - minutesPassed;

        // Nếu thời gian còn lại từ 0 đến 15 phút thì bật cảnh báo
        return minutesRemaining >= 0 && minutesRemaining <= 15;
    }
}