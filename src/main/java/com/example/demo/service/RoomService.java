package com.example.demo.service;

import com.example.demo.model.Room;
import com.example.demo.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    // ===== LẤY DANH SÁCH =====
    public List<Room> findAll() {
        return repository.findAll();
    }

    // ===== THÊM METHOD GETALL THEO YÊU CẦU =====
    public List<Room> getAll() {
        return repository.findAll();
    }

    // ===== KIỂM TRA TÊN PHÒNG TỒN TẠI =====
    public boolean existsByRoomName(String roomName) {
        return repository.existsByRoomName(roomName);
    }

    // ===== TÌM THEO ID =====
    public Room findById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    // ===== LƯU =====
    public void save(Room room) {
        repository.save(room);
    }

    // ===== XÓA =====
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // ===== TÌM KIẾM =====
    public List<Room> search(Double price) {
        return repository.search(price);
    }
}