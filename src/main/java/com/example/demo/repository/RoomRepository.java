package com.example.demo.repository;

import com.example.demo.enumtype.RoomStatus;
import com.example.demo.model.Room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // ===== SEARCH THEO GIÁ =====

    @Query("""
            SELECT r FROM Room r
            WHERE
                (:price IS NULL OR r.price = :price)
            """)
    List<Room> search(@Param("price") Double price);

    // ===== TÌM THEO TRẠNG THÁI =====

    List<Room> findByStatus(RoomStatus status);

    // ===== TÌM THEO LOẠI =====

    List<Room> findByRoomType(String roomType);

    // ===== TÌM THEO TÊN =====

    List<Room> findByRoomNameContainingIgnoreCase(String keyword);

    // ===== SEARCH NÂNG CAO (ĐÃ CẬP NHẬT THEO YÊU CẦU) =====

    @Query("""
        SELECT r FROM Room r
        WHERE
            (
                :keyword IS NULL
    
                OR LOWER(r.roomName)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
    
                OR LOWER(r.roomType)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
    
                OR LOWER(r.description)
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
    
        AND
            (:status IS NULL OR r.status = :status)
    
        AND
            (:roomType IS NULL OR r.roomType = :roomType)
    """)
    List<Room> searchRooms(
            @Param("keyword") String keyword,
            @Param("status") RoomStatus status,
            @Param("roomType") String roomType
    );
}