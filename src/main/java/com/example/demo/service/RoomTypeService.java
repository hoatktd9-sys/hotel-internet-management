package com.example.demo.service;

import com.example.demo.model.RoomType;
import com.example.demo.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomTypeService {

    private final RoomTypeRepository repository;

    public RoomTypeService(RoomTypeRepository repository) {
        this.repository = repository;
    }

    public List<RoomType> findAll() {
        return repository.findAll();
    }

    public RoomType findById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    public void save(RoomType roomType) {
        repository.save(roomType);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
