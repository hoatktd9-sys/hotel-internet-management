package com.example.demo.service;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Room;
import com.example.demo.repository.RoomRepository;
import org.springframework.stereotype.Service;
import com.example.demo.repository.RoomRepository;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomrepository;
    private final RoomTypeService roomTypeService;

    public RoomService(RoomRepository repository, RoomTypeService roomTypeService) {
        this.roomrepository = repository;
        this.roomTypeService = roomTypeService;
    }

    // ===== LẤY TOÀN BỘ PHÒNG =====
    public List<Room> findAll() {
        return roomrepository.findAll();
    }

    // ===== GET ALL =====
    public List<Room> getAll() {
        return roomrepository.findAll();
    }

    // ===== KIỂM TRA TÊN PHÒNG TỒN TẠI =====
    public boolean existsByRoomName(String roomName) {
        return roomrepository.existsByRoomName(roomName);
    }

    // ===== TÌM THEO ID =====
    public Room findById(Long id) {
        return roomrepository.findById(id).orElseThrow();
    }

    // ===== LƯU PHÒNG =====
    public void save(Room room) {
        roomrepository.save(room);
    }

    // ===== XÓA PHÒNG =====
    public void delete(Long id) {
        roomrepository.deleteById(id);
    }

    // ===== SEARCH THEO GIÁ =====
    public List<Room> search(Double price) {
        return roomrepository.search(price);
    }

    // ===== TÌM THEO TRẠNG THÁI =====
    public List<Room> findByStatus(RoomStatus status) {
        return roomrepository.findByStatus(status);
    }

    // ===== TÌM THEO LOẠI PHÒNG =====
    public List<Room> findByRoomType(String roomType) {
        return roomrepository.findByRoomType(roomType);
    }

    // ===== TÌM THEO TÊN PHÒNG =====
    public List<Room> searchByName(String keyword) {
        return roomrepository.findByRoomNameContainingIgnoreCase(keyword);
    }

    // ===== ĐỔI TRẠNG THÁI =====
    public void updateStatus(Long id, RoomStatus status) {

        Room room = findById(id);

        room.setStatus(status);

        roomrepository.save(room);
    }

    // ===== SEARCH NÂNG CAO (MỚI THÊM) =====
    public List<Room> searchRooms(
            String keyword,
            RoomStatus status,
            String roomType
    ) {

        return roomrepository.searchRooms(
                keyword,
                status,
                roomType
        );
    }
}