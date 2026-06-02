package com.example.demo.model;

import com.example.demo.enumtype.RoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
@SoftDelete // Kích hoạt xóa mềm Hibernate 7 (Tự động thêm & quản lý cột 'deleted')
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= THÔNG TIN PHÒNG =================

    @NotBlank(message = "Tên phòng không được để trống")
    @Column(name = "room_name", unique = true, nullable = false)
    private String roomName;

    @NotNull(message = "Giá phòng không được để trống")
    @DecimalMin(value = "10000.0", message = "Giá phòng phải lớn hơn 50.000 VNĐ")
    @Column(nullable = false)
    private Double price;

    @NotNull(message = "Số máy tính không được để trống")
    @Min(value = 1, message = "Số máy tính phải lớn hơn 0")
    @Column(name = "computer_count", nullable = false)
    private Integer computerCount;

    @NotBlank(message = "Mô tả không được để trống")
    @Column(columnDefinition = "TEXT")
    private String description;

    private String image;

    // ================= LOẠI PHÒNG =================

    @NotNull(message = "Loại phòng không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    // ================= CẤU HÌNH MÁY =================

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

    // ================= TRẠNG THÁI =================

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    // ================= CHECK IN =================
    // Bỏ CascadeType.ALL và orphanRemoval để tránh xóa nhầm lịch sử CheckIn khi xóa
    // phòng
    @OneToMany(mappedBy = "room")
    private List<CheckIn> checkIns = new ArrayList<>();

    @Transient
    public CheckIn getActiveCheckIn() {
        if (checkIns == null)
            return null;
        for (CheckIn c : checkIns) {
            if ("ACTIVE".equals(c.getStatus())) {
                return c;
            }
        }
        return null;
    }

    @Transient
    public CheckIn getReservedCheckIn() {
        if (checkIns == null)
            return null;
        for (CheckIn c : checkIns) {
            if ("RESERVED".equals(c.getStatus())) {
                return c;
            }
        }
        return null;
    }

    // ================= CONSTRUCTOR =================

    public Room() {
        this.status = RoomStatus.AVAILABLE;
    }
}