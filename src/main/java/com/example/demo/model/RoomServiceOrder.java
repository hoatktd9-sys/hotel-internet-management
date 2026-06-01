package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_service_orders")
@Getter
@Setter
public class RoomServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với Phòng gọi món
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; // Hãy đảm bảo bạn đã có class Room.java trong dự án

    // Liên kết với Sản phẩm/Dịch vụ được chọn
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity; // Số lượng món đặt

    @Column(nullable = false)
    private Double totalPrice; // Giá tạm tính = quantity * product.price

    private String status; // Trạng thái: "PENDING" (Chờ cung cấp), "DELIVERED" (Đã phục vụ), "CANCELLED" (Hủy)

    private LocalDateTime orderTime;

    @PrePersist
    protected void onCreate() {
        this.orderTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
}