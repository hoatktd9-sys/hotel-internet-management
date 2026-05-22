package com.example.demo.repository;

import com.example.demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Tìm kiếm không phân biệt hoa thường theo Tên hoặc chứa Số điện thoại
    List<Customer> findByFullNameContainingIgnoreCaseOrPhoneNumberContaining(
            String fullName,
            String phoneNumber
    );
}