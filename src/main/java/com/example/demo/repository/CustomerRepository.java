package com.example.demo.repository;

import com.example.demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    List<Customer> findByFullNameContainingIgnoreCaseOrPhoneNumberContaining(
            String fullName,
            String phoneNumber
    );
}