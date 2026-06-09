package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Column(nullable = false, unique = true)
    private String name;

    private String contactName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    private String email;
    private String address;

    @Column(nullable = false)
    private Boolean active = true;
}