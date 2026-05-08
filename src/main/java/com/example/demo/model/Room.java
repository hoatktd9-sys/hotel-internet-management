package com.example.demo.model;

import com.example.demo.enumtype.RoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên phòng không được để trống")
    private String roomName;

    @NotNull(message = "Giá phòng không được để trống")
    @Min(value = 50000, message = "Giá phòng phải lớn hơn 50.000 VNĐ")
    private Double price;

    @NotNull(message = "Số máy tính không được để trống")
    @Min(value = 1, message = "Số máy tính phải lớn hơn 0")
    private Integer computerCount;

    @NotBlank(message = "Loại phòng không được để trống")
    private String roomType;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    // ===== Constructor =====

    public Room() {
        this.status = RoomStatus.AVAILABLE;
    }

    // ===== Getter Setter =====

    public Long getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getComputerCount() {
        return computerCount;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getDescription() {
        return description;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setComputerCount(Integer computerCount) {
        this.computerCount = computerCount;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
}