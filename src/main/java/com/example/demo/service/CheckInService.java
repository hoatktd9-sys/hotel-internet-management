package com.example.demo.service;

import com.example.demo.model.CheckIn;
import com.example.demo.repository.CheckInRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // ===== CHECK-IN ĐANG HOẠT ĐỘNG =====

    public CheckIn findActiveByRoomId(Long roomId){
        CheckIn checkIn = checkInRepository
                .findByRoomIdAndStatus(roomId, "ACTIVE")
                .orElse(null);
        if (checkIn == null) {
            checkIn = checkInRepository
                    .findByRoomIdAndCheckOutTimeIsNull(roomId)
                    .orElse(null);
            if (checkIn != null) {
                checkIn.setStatus("ACTIVE");
                checkInRepository.save(checkIn);
            }
        }
        return checkIn;
    }

    // ===== ĐẶT TRƯỚC ĐANG HOẠT ĐỘNG =====

    public CheckIn findReservedByRoomId(Long roomId){

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