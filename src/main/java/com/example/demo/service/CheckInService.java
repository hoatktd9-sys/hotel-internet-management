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
                .orElseThrow();
    }

    // ===== TÌM CHECK-IN ĐANG HOẠT ĐỘNG THEO PHÒNG =====
    public CheckIn findActiveByRoomId(Long roomId){
        return checkInRepository
                .findByRoomIdAndCheckOutTimeIsNull(roomId);
    }

    // ===== LƯU =====
    public void save(CheckIn checkIn){
        checkInRepository.save(checkIn);
    }

}