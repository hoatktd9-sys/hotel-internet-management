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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id));
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
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findAll();
        }
        return repository.findByFullNameContainingIgnoreCaseOrPhoneNumberContaining(
                keyword.trim(),
                keyword.trim()
        );
    }

    // ===== ĐÁNH DẤU / HỦY VIP (MỚI THÊM) =====
    public void toggleVip(Long id) {
        Customer customer = findById(id);
        customer.setVip(!customer.isVip()); // Đảo ngược trạng thái hiện tại
        repository.save(customer);
    }

    // ===== KHÓA / MỞ KHÓA KHÁCH HÀNG (MỚI THÊM) =====
    public void toggleActive(Long id) {
        Customer customer = findById(id);
        customer.setActive(!customer.isActive()); // Đảo ngược trạng thái hoạt động
        repository.save(customer);
    }
}