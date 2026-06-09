package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với sản phẩm được biến động kho
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Liên kết với nhà cung cấp (chỉ áp dụng khi NHẬP KHO, XUẤT KHO có thể để null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private String transactionType; // "IMPORT" (Nhập kho), "EXPORT" (Xuất kho)

    @Column(nullable = false)
    private Integer quantity; // Số lượng biến động

    private Double price; // Giá nhập/xuất trên một đơn vị sản phẩm

    private String note; // Lý do nhập, hoặc mã phòng gọi món (ví dụ: "Phòng 101 gọi món")

    private LocalDateTime transactionTime;

    private String operator; // Tên nhân viên thực hiện (lấy từ Spring Security)

    @PrePersist
    protected void onCreate() {
        this.transactionTime = LocalDateTime.now();
    }
}