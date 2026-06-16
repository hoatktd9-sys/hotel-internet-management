package com.example.demo.repository;

import com.example.demo.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    // Tìm các log mới nhất xếp lên đầu
    List<ActivityLog> findAllByOrderByTimestampDesc();

    // Tìm các log mới nhất xếp lên đầu có phân trang
    Page<ActivityLog> findAllByOrderByTimestampDesc(Pageable pageable);
}