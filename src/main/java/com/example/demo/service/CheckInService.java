package com.example.demo.service;

import com.example.demo.model.CheckIn;
import com.example.demo.repository.CheckInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; // THÊM IMPORT NÀY ĐỂ HẾT BÁO ĐỎ OPTIONAL

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
                .orElseThrow();
    }

    // ===== CHECK-IN ĐANG HOẠT ĐỘNG (ĐÃ CẬP NHẬT CHỐNG KẸT LUỒNG DỮ LIỆU CŨ) =====
    public CheckIn findActiveByRoomId(Long roomId){
        // 1. Tìm phiên chuẩn (chưa check-out)
        Optional<CheckIn> activeCheckIn = checkInRepository.findByRoomIdAndCheckOutTimeIsNull(roomId);

        if (activeCheckIn.isPresent()) {
            return activeCheckIn.get();
        }

        // 2. Nếu dữ liệu cũ dính lỗi, tự động lấy bản ghi mới nhất của phòng đó để cứu luồng
        return checkInRepository.findFirstByRoomIdOrderByCheckInTimeDesc(roomId).orElse(null);
    }

    // ===== LƯU =====
    public void save(CheckIn checkIn){
        checkInRepository.save(checkIn);
    }
}