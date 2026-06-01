package com.example.demo.service;

import com.example.demo.model.CheckIn;
import com.example.demo.repository.CheckInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CheckInService {

    @Autowired
    private CheckInRepository checkInRepository;

    // ===== LẤY TOÀN BỘ =====
    public List<CheckIn> getAll(){
        return checkInRepository.findAll();
    }

    // ===== TÌM THEO ID =====
    public CheckIn findById(Long id){
        return checkInRepository
                .findById(id)
                .orElse(null);
    }

    // ===== CHECK-IN ĐANG HOẠT ĐỘNG (ĐÃ CẬP NHẬT CHỐNG KẸT LUỒNG DỮ LIỆU CŨ) =====
    public CheckIn findActiveByRoomId(Long roomId){
        // 1. Ưu tiên tìm phiên chuẩn theo trạng thái ACTIVE
        CheckIn checkIn = checkInRepository
                .findByRoomIdAndStatus(roomId, "ACTIVE")
                .orElse(null);

        // 2. Nếu không thấy, quét tìm theo cơ chế CheckOutTime bằng NULL của bạn để cứu luồng dữ liệu cũ
        if (checkIn == null) {
            checkIn = checkInRepository
                    .findByRoomIdAndCheckOutTimeIsNull(roomId)
                    .orElse(null);

            // Nếu cứu được bản ghi cũ, đồng bộ trạng thái về ACTIVE luôn cho mượt mà
            if (checkIn != null) {
                checkIn.setStatus("ACTIVE");
                checkInRepository.save(checkIn);
            }
        }

        // 3. Biện pháp phòng ngự cuối cùng: Nếu vẫn trống, lấy bản ghi mới nhất của phòng đó
        if (checkIn == null) {
            checkIn = checkInRepository.findFirstByRoomIdOrderByCheckInTimeDesc(roomId).orElse(null);
        }

        return checkIn;
    }

    // ===== ĐẶT TRƯỚC ĐANG HOẠT ĐỘNG =====
    public CheckIn findReservedByRoomId(Long roomId){
        // Tìm đúng bản ghi phòng đang được giữ chỗ theo trạng thái RESERVED của đồng đội
        return checkInRepository
                .findByRoomIdAndStatus(roomId, "RESERVED")
                .orElse(null);
    }

    // ===== LẤY THEO TRẠNG THÁI =====
    public List<CheckIn> findByStatus(String status){
        return checkInRepository.findByStatus(status);
    }

    // ===== LƯU =====
    public void save(CheckIn checkIn){
        checkInRepository.save(checkIn);
    }
}