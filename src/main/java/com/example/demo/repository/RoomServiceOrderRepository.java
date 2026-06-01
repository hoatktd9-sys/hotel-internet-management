package com.example.demo.repository;

import com.example.demo.model.RoomServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomServiceOrderRepository extends JpaRepository<RoomServiceOrder, Long> {
    // Tìm các đơn gọi món của một phòng cụ thể mà chưa thanh toán/đang chờ duyệt
    List<RoomServiceOrder> findByRoomIdAndStatus(Long roomId, String status);
}