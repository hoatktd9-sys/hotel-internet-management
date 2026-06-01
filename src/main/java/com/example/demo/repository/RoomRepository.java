package com.example.demo.repository;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // ===== HÀM CẬP NHẬT TRẠNG THÁI CÔ LẬP =====
    // Cập nhật trực tiếp trường status theo id mà không can thiệp các trường khác
    @Transactional
    @Modifying
    @Query("UPDATE Room r SET r.status = :status WHERE r.id = :id")
    void updateRoomStatusOnly(@Param("id") Long id, @Param("status") RoomStatus status);

    // ===== CÁC HÀM CŨ =====
    boolean existsByRoomName(String roomName);

    // ===== SEARCH THEO GIÁ PHÒNG =====
    @Query("""
            SELECT r FROM Room r
            WHERE
                (:price IS NULL OR r.price = :price)
            """)
    List<Room> search(@Param("price") Double price);

    List<Room> findByStatus(RoomStatus status);

    List<Room> findByRoomNameContainingIgnoreCase(String keyword);

    @Query("""
        SELECT r FROM Room r 
        WHERE (:keyword IS NULL OR LOWER(r.roomName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR r.status = :status)
          AND (:roomType IS NULL OR r.roomType = :roomType)
    """)
    List<Room> searchRooms(
            @Param("keyword") String keyword,
            @Param("status") RoomStatus status,
            @Param("roomType") String roomType
    );

    List<Room> findByRoomType(String roomType);
}