package com.example.demo.repository;

import com.example.demo.model.RoomServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RoomServiceOrderRepository extends JpaRepository<RoomServiceOrder, Long> {

    // 1. JPQL lấy danh sách order hợp lệ, tự động lọc sạch các thực thể không tồn tại
    @Query("SELECT o FROM RoomServiceOrder o " +
            "JOIN o.product p " +
            "JOIN o.room r " +
            "WHERE r.id IS NOT NULL")
    List<RoomServiceOrder> findAllValidOrders();

    // 2. JPQL tìm kiếm danh sách theo ID phòng máy và trạng thái
    @Query("SELECT o FROM RoomServiceOrder o WHERE o.room.id = :roomId AND o.status = :status")
    List<RoomServiceOrder> findByRoomIdAndStatus(@Param("roomId") Long roomId, @Param("status") String status);
}