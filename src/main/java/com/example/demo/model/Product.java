package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @Min(value = 0, message = "Giá sản phẩm không được nhỏ hơn 0")
    @Column(nullable = false)
    private Double price;

    @NotNull(message = "Số lượng kho không được để trống")
    @Min(value = 0, message = "Số lượng không được nhỏ hơn 0")
    @Column(nullable = false)
    private Integer stockQuantity;

    private String image; // Lưu đường dẫn hoặc tên file ảnh sản phẩm

    private String description;

    // Thiết lập mối quan hệ Nhiều sản phẩm thuộc về Một danh mục
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;
    // BỔ SUNG CHO TÍNH NĂNG XÓA MỀM (FEATURE 37)
    @Column(nullable = false)
    private Boolean active = true; // Mặc định sản phẩm tạo ra sẽ ở trạng thái kinh doanh (true)
}