package com.example.demo.model;

import com.example.demo.enumtype.RoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên phòng không được để trống")
    @Column(unique = true)
    private String roomName;

    @NotNull(message = "Giá phòng không được để trống")
    @DecimalMin(value = "50000.0", message = "Giá phòng phải lớn hơn 50.000 VNĐ")
    private Double price;

    @NotNull(message = "Số máy tính không được để trống")
    @Min(value = 1, message = "Số máy tính phải lớn hơn 0")
    private Integer computerCount;

    @NotNull(message = "Loại phòng không được để trống")
    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    private String image;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotBlank(message = "CPU không được để trống")
    private String cpu;

    @NotBlank(message = "RAM không được để trống")
    private String ram;

    @NotBlank(message = "VGA không được để trống")
    private String vga;

    @NotBlank(message = "SSD không được để trống")
    private String ssd;

    @NotBlank(message = "Màn hình không được để trống")
    private String monitor;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
    private java.util.List<CheckIn> checkIns;

    public Room() {
        this.status = RoomStatus.AVAILABLE;
    }

    public Room(Long id, String roomName, Double price, Integer computerCount, RoomType roomType, String image, String description, String cpu, String ram, String vga, String ssd, String monitor, RoomStatus status, List<CheckIn> checkIns) {
        this.id = id;
        this.roomName = roomName;
        this.price = price;
        this.computerCount = computerCount;
        this.roomType = roomType;
        this.image = image;
        this.description = description;
        this.cpu = cpu;
        this.ram = ram;
        this.vga = vga;
        this.ssd = ssd;
        this.monitor = monitor;
        this.status = status;
        this.checkIns = checkIns;
    }

    public String getSsd() {
        return ssd;
    }

    public void setSsd(String ssd) {
        this.ssd = ssd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getComputerCount() {
        return computerCount;
    }

    public void setComputerCount(Integer computerCount) {
        this.computerCount = computerCount;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getVga() {
        return vga;
    }

    public void setVga(String vga) {
        this.vga = vga;
    }

    public String getMonitor() {
        return monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public List<CheckIn> getCheckIns() {
        return checkIns;
    }

    public void setCheckIns(List<CheckIn> checkIns) {
        this.checkIns = checkIns;
    }
}