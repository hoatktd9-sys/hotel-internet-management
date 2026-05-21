package com.example.demo.service;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Room;
import com.example.demo.repository.CheckInRepository;
import com.example.demo.repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeService roomTypeService;
    private final CheckInRepository checkInRepository;

    public RoomService(
            RoomRepository roomRepository,
            RoomTypeService roomTypeService,
            CheckInRepository checkInRepository
    ) {

        this.roomRepository = roomRepository;
        this.roomTypeService = roomTypeService;
        this.checkInRepository = checkInRepository;
    }

    // ===== LẤY TOÀN BỘ =====

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    // ===== TÌM THEO ID =====

    public Room findById(Long id) {

        return roomRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy phòng"));
    }

    // ===== KIỂM TRA TÊN =====

    public boolean existsByRoomName(String roomName) {
        return roomRepository.existsByRoomName(roomName);
    }

    // ===== LƯU =====

    public void save(Room room) {
        roomRepository.save(room);
    }

    // ===== XÓA MỀM =====
    @Transactional
    public void delete(Long id) {
        // 1. Kiểm tra xem phòng này hiện tại có lượt Check-in nào đang hoạt động (chưa check-out) không
        boolean isRoomOccupied = checkInRepository.findByRoomIdAndCheckOutTimeIsNull(id).isPresent();

        if (isRoomOccupied) {
            throw new RuntimeException("Không thể xóa phòng này! Phòng hiện đang có khách đang sử dụng (Chưa hoàn tất Check-out).");
        }

        // 2. Nếu phòng trống (không có khách đang ngồi), tiến hành xóa mềm
        // Nhờ có @SoftDelete ở Entity Room, lệnh này sẽ tự động chuyển thành UPDATE room SET deleted = true WHERE id = ?
        roomRepository.deleteById(id);
    }

    // ===== SEARCH GIÁ =====

    public List<Room> search(Double price) {
        return roomRepository.search(price);
    }

    // ===== TÌM THEO STATUS =====

    public List<Room> findByStatus(RoomStatus status) {
        return roomRepository.findByStatus(status);
    }

    // ===== TÌM THEO ROOM TYPE =====

    public List<Room> findByRoomType(String roomType) {
        return roomRepository.findByRoomType(roomType);
    }

    // ===== SEARCH TÊN =====

    public List<Room> searchByName(String keyword) {

        return roomRepository
                .findByRoomNameContainingIgnoreCase(keyword);
    }

    // ===== UPDATE STATUS =====

    public void updateStatus(
            Long id,
            RoomStatus status
    ) {

        Room room = findById(id);

        room.setStatus(status);

        roomRepository.save(room);
    }

    // ===== SEARCH NÂNG CAO =====

    public List<Room> searchRooms(
            String keyword,
            RoomStatus status,
            String roomType
    ) {

        return roomRepository.searchRooms(
                keyword,
                status,
                roomType
        );
    }
}