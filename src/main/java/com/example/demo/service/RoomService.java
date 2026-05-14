package com.example.demo.service;

import com.example.demo.enumtype.RoomStatus;
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

    // ===== LẤY TOÀN BỘ PHÒNG =====
    public List<Room> findAll() {
        return repository.findAll();
    }

    // ===== GET ALL =====
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

    // ===== LƯU PHÒNG =====
    public void save(Room room) {
        repository.save(room);
    }

    // ===== XÓA PHÒNG =====
    public void delete(Long id) {
        repository.deleteById(id);
    }

    // ===== SEARCH THEO GIÁ =====
    public List<Room> search(Double price) {
        return repository.search(price);
    }

    // ===== TÌM THEO TRẠNG THÁI =====
    public List<Room> findByStatus(RoomStatus status) {
        return repository.findByStatus(status);
    }

    // ===== TÌM THEO LOẠI PHÒNG =====
    public List<Room> findByRoomType(String roomType) {
        return repository.findByRoomType(roomType);
    }

    // ===== TÌM THEO TÊN PHÒNG =====
    public List<Room> searchByName(String keyword) {
        return repository.findByRoomNameContainingIgnoreCase(keyword);
    }

    // ===== ĐỔI TRẠNG THÁI =====
    public void updateStatus(Long id, RoomStatus status) {

        Room room = findById(id);

        room.setStatus(status);

        repository.save(room);
    }

    // ===== SEARCH NÂNG CAO (MỚI THÊM) =====
    public List<Room> searchRooms(
            String keyword,
            RoomStatus status,
            String roomType
    ) {

        return repository.searchRooms(
                keyword,
                status,
                roomType
        );
    }
}