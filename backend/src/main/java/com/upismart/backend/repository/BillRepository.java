package com.upismart.backend.repository;

import com.upismart.backend.entity.BillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<BillEntity, Long> {
    List<BillEntity> findByUserIdOrderByDueDayAsc(Long userId);
}
