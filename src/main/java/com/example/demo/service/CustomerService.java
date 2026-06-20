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

    // ===== TÌM KIẾM PHÂN TRANG =====
    public org.springframework.data.domain.Page<Customer> search(String keyword, org.springframework.data.domain.Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findAll(pageable);
        }
        String kw = keyword.trim();
        return repository.findByFullNameContainingIgnoreCaseOrPhoneNumberContainingOrIdentityNumberContaining(
                kw, kw, kw, pageable
        );
    }

    // ===== 1. ĐÁNH DẤU / HỦY VIP (ĐÃ SỬA HẾT LỖI BÁO ĐỎ) =====
    public void toggleVip(Long id) {
        Customer customer = findById(id);

        boolean currentVip = false;
        try {
            // Nếu dữ liệu cũ trong DB bị lỗi Null, khối catch sẽ bắt lại
            currentVip = customer.isVip();
        } catch (Exception e) {
            currentVip = false;
        }

        customer.setVip(!currentVip);
        repository.save(customer);
    }

    // ===== 2. ĐÁNH DẤU / HỦY KÍCH HOẠT (ĐÃ SỬA HẾT LỖI BÁO ĐỎ) =====
    public void toggleActive(Long id) {
        Customer customer = findById(id);

        boolean currentActive = false;
        try {
            // Nếu dữ liệu cũ trong DB bị lỗi Null, khối catch sẽ bắt lại
            currentActive = customer.isActive();
        } catch (Exception e) {
            currentActive = false;
        }

        customer.setActive(!currentActive);
        repository.save(customer);
    }
}