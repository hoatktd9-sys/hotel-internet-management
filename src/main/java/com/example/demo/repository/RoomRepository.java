package com.example.demo.repository;

import com.example.demo.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomName(String roomName);

    // ===== SEARCH THEO GIÁ PHÒNG =====
    @Query("""
            SELECT r FROM Room r
            WHERE
                (:price IS NULL OR r.price = :price)
            """)
    List<Room> search(@Param("price") Double price);
}