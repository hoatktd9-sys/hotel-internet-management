package com.example.demo.repository;

import com.example.demo.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    // Tìm các nhà cung cấp đang hoạt động
    List<Supplier> findByActiveTrue();

    // Kiểm tra tồn tại phục vụ Thêm mới nhanh
    boolean existsByName(String name);

    // Tìm kiếm chính xác phục vụ kiểm tra trùng lặp khi Update dữ liệu
    Optional<Supplier> findByName(String name);
}