package com.example.demo.repository;

import com.example.demo.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    // Tìm lịch sử sắp xếp theo thời gian mới nhất lên đầu
    List<InventoryTransaction> findAllByOrderByTransactionTimeDesc();
    // Lấy toàn bộ lịch sử giao dịch, giao dịch mới nhất xếp lên đầu
    List<InventoryTransaction> findAllByOrderByIdDesc();
}