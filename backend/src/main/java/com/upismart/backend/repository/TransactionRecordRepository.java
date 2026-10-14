package com.upismart.backend.repository;

import com.upismart.backend.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<TransactionEntity> findByReferenceId(String referenceId);
    List<TransactionEntity> findByUserIdAndCategory(Long userId, String category);
}
