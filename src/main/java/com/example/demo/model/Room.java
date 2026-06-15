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
@SoftDelete
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên phòng không được để trống")
    @Column(name = "room_name", unique = true, nullable = false)
    private String roomName;

    @NotNull(message = "Giá phòng không được để trống")
    @DecimalMin(value = "10000.0", message = "Giá phòng phải lớn hơn 10.000 VNĐ")
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

    @NotNull(message = "Loại phòng không được để trống")
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

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

    // FIX LỖI 5: Ép độ dài cột VARCHAR trên MySQL rộng ra 20 ký tự để chứa vừa chữ RESERVED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RoomStatus status;

    @OneToMany(mappedBy = "room")
    private List<CheckIn> checkIns = new ArrayList<>();

    @Transient
    public CheckIn getActiveCheckIn() {
        if (checkIns == null) return null;
        for (CheckIn c : checkIns) {
            if ("ACTIVE".equals(c.getStatus())) {
                return c;
            }
        }
        return null;
    }

    @Transient
    public CheckIn getReservedCheckIn() {
        if (checkIns == null) return null;
        for (CheckIn c : checkIns) {
            if ("RESERVED".equals(c.getStatus())) {
                return c;
            }
        }
        return null;
    }

    public Room() {
        this.status = RoomStatus.AVAILABLE;
    }
}