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

    // lấy toàn bộ khách hàng
    public List<Customer> findAll() {
        return repository.findAll();
    }

    // Thêm method getAll theo yêu cầu
    public List<Customer> getAll() {
        return repository.findAll();
    }

    // lưu khách hàng
    public void save(Customer customer) {
        repository.save(customer);
    }
}