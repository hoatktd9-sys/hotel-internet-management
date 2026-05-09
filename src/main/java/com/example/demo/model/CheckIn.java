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

    // ===== GETTER SETTER =====

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
}