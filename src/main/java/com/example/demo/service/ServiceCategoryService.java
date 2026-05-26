package com.example.demo.service;

import com.example.demo.model.ServiceCategory;
import com.example.demo.repository.ServiceCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceCategoryService {

    private final ServiceCategoryRepository repository;

    public ServiceCategoryService(ServiceCategoryRepository repository) {
        this.repository = repository;
    }

    public List<ServiceCategory> findAll() {
        return repository.findAll();
    }

    public ServiceCategory findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public ServiceCategory save(ServiceCategory category) {
        return repository.save(category);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return repository.findByName(name).isPresent();
    }
}