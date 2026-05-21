package com.example.demo.service;

import com.example.demo.model.Customer;
import com.example.demo.repository.CustomerRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    // ===== LẤY TẤT CẢ =====

    public List<Customer> findAll() {
        return repository.findAll();
    }

    // ===== TÌM THEO ID =====

    public Customer findById(Long id) {

        return repository.findById(id)
                .orElseThrow();

    }

    // ===== LƯU =====

    public void save(Customer customer) {

        repository.save(customer);

    }

    // ===== XÓA =====

    public void delete(Long id) {

        repository.deleteById(id);

    }

    // ===== TÌM KIẾM (MỚI THÊM) =====

    public List<Customer> search(String keyword) {

        // nếu rỗng -> trả toàn bộ
        if (keyword == null || keyword.trim().isEmpty()) {

            return repository.findAll();

        }

        return repository
                .findByFullNameContainingIgnoreCaseOrPhoneNumberContaining(
                        keyword,
                        keyword
                );

    }

}