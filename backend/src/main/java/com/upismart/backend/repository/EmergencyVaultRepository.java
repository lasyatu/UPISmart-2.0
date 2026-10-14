package com.upismart.backend.repository;

import com.upismart.backend.entity.EmergencyVaultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmergencyVaultRepository extends JpaRepository<EmergencyVaultEntity, Long> {
    Optional<EmergencyVaultEntity> findByUserId(Long userId);
}
