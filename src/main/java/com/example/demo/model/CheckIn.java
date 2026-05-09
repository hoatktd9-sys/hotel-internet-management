package com.example.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkin_room")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== KHÁCH =====
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // ===== PHÒNG =====
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    // ===== THỜI GIAN CHECK-IN =====
    private LocalDateTime checkInTime;

    // ===== THỜI GIAN CHECK-OUT =====
    private LocalDateTime checkOutTime;

    // ===== TỔNG SỐ GIỜ =====
    private Double totalHours;

    // ===== TỔNG TIỀN (MỚI THÊM) =====
    private Double totalPrice;

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

    // GETTER CHO TỔNG TIỀN
    public Double getTotalPrice() {
        return totalPrice;
    }

    // SETTER CHO TỔNG TIỀN
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}