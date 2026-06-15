package com.example.demo.repository;

import com.example.demo.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // Sử dụng JPQL dựa trên tên thực thể Class Bill, CheckIn, Room để Hibernate tự map tên bảng
    @Query("SELECT b FROM Bill b " +
            "JOIN b.checkIn c " +
            "JOIN c.room r " +
            "WHERE r.id IS NOT NULL")
    List<Bill> findAllValidBills();

    @Query("SELECT b FROM Bill b WHERE b.checkIn.id = :checkInId")
    Optional<Bill> findByCheckInId(@Param("checkInId") Long checkInId);
}