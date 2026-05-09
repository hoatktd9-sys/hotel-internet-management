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

    // LẤY TOÀN BỘ CHECK-IN
    public List<CheckIn> getAll(){
        return checkInRepository.findAll();
    }

    // LƯU CHECK-IN
    public void save(CheckIn checkIn){
        checkInRepository.save(checkIn);
    }

}