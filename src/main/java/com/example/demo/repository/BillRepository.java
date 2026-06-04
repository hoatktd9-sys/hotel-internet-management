package com.example.demo.repository;

import com.example.demo.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    // Tìm kiếm hóa đơn theo mã hóa đơn (Phục vụ tra cứu/in ấn)
    Optional<Bill> findByBillCode(String billCode);

    // Tìm kiếm hóa đơn dựa trên mã phiên thuê phòng
    Optional<Bill> findByCheckInId(Long checkInId);
}