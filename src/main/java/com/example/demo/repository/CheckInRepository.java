package com.example.demo.repository;

import com.example.demo.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CheckInRepository
        extends JpaRepository<CheckIn, Long> {

    // ===== XÓA THEO ROOM =====
    void deleteByRoomId(Long roomId);

    // ===== CHECK-IN ĐANG HOẠT ĐỘNG =====
    Optional<CheckIn> findByRoomIdAndCheckOutTimeIsNull(Long roomId);

    // ===== LỊCH SỬ PHÒNG =====
    List<CheckIn> findByRoomId(Long roomId);
}