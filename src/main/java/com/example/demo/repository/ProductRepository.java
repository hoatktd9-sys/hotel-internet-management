package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    List<Product> findByActiveTrue();

    // BỔ SUNG MỚI: Tìm sản phẩm theo tên và trạng thái hoạt động (Phục vụ check trùng tên chính xác)
    Optional<Product> findByNameAndActiveTrue(String name);
}