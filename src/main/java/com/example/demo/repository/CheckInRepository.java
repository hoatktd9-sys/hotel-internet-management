package com.example.demo.repository;

import com.example.demo.model.CheckIn;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepository
        extends JpaRepository<CheckIn, Long> {

    // tìm check-in đang hoạt động theo room id
    CheckIn findByRoomIdAndCheckOutTimeIsNull(Long roomId);

}