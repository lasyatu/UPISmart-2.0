package com.upismart.backend.repository;

import com.upismart.backend.entity.VaultAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaultAuditLogRepository extends JpaRepository<VaultAuditLogEntity, Long> {
    List<VaultAuditLogEntity> findByVaultIdOrderByCreatedAtDesc(Long vaultId);
}
