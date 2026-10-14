package com.upismart.backend.service;

import com.upismart.backend.core.exception.InsufficientBalanceException;
import com.upismart.backend.core.exception.InvalidAmountException;
import com.upismart.backend.core.exception.InvalidPinException;
import com.upismart.backend.core.exception.VaultLockedException;
import com.upismart.backend.core.vault.EmergencyVault;
import com.upismart.backend.dto.VaultActionRequest;
import com.upismart.backend.dto.VaultStatusResponse;
import com.upismart.backend.entity.EmergencyVaultEntity;
import com.upismart.backend.entity.UserEntity;
import com.upismart.backend.entity.VaultAuditLogEntity;
import com.upismart.backend.repository.EmergencyVaultRepository;
import com.upismart.backend.repository.VaultAuditLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VaultService {

    private final EmergencyVaultRepository vaultRepository;
    private final VaultAuditLogRepository auditLogRepository;
    private final PaymentService paymentService;
    private final String defaultDemoPin;

    public VaultService(EmergencyVaultRepository vaultRepository,
                        VaultAuditLogRepository auditLogRepository,
                        PaymentService paymentService,
                        @Value("${app.demo.demo-pin:1234}") String defaultDemoPin) {
        this.vaultRepository = vaultRepository;
        this.auditLogRepository = auditLogRepository;
        this.paymentService = paymentService;
        this.defaultDemoPin = defaultDemoPin;
    }

    public VaultStatusResponse getVaultStatus() {
        EmergencyVaultEntity vault = getOrCreateVault();
        boolean isLockedOut = vault.getLockedUntil() != null && vault.getLockedUntil().isAfter(LocalDateTime.now());
        List<VaultAuditLogEntity> logs = auditLogRepository.findByVaultIdOrderByCreatedAtDesc(vault.getVaultId());

        List<VaultStatusResponse.VaultAuditLogDto> logDtos = logs.stream()
                .limit(15)
                .map(l -> new VaultStatusResponse.VaultAuditLogDto(
                        l.getActionType(),
                        l.getAmount(),
                        Boolean.TRUE.equals(l.getIsSuccessful()),
                        l.getFailureReason(),
                        l.getResultingBalance(),
                        l.getCreatedAt() != null ? l.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a")) : ""
                ))
                .collect(Collectors.toList());

        int remainingAttempts = Math.max(0, EmergencyVault.MAX_FAILED_ATTEMPTS - (vault.getFailedAttempts() != null ? vault.getFailedAttempts() : 0));
        String statusMsg = isLockedOut ? "Vault is temporarily locked for security" : (Boolean.TRUE.equals(vault.getIsLocked()) ? "Locked" : "Unlocked");

        return new VaultStatusResponse(
                vault.getVaultId(),
                vault.getReserveBalance(),
                Boolean.TRUE.equals(vault.getIsLocked()) || isLockedOut,
                remainingAttempts,
                statusMsg,
                logDtos
        );
    }

    @Transactional
    public VaultStatusResponse deposit(VaultActionRequest request) throws InvalidAmountException {
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw new InvalidAmountException("Deposit amount must be strictly greater than 0.",
                    request.getAmount() != null ? request.getAmount().doubleValue() : 0.0);
        }

        EmergencyVaultEntity vault = getOrCreateVault();
        EmergencyVault coreVault = new EmergencyVault(vault.getReserveBalance().doubleValue(), defaultDemoPin);
        double newBal = coreVault.deposit(request.getAmount().doubleValue());

        vault.setReserveBalance(BigDecimal.valueOf(newBal));
        vaultRepository.save(vault);

        recordAuditLog(vault.getVaultId(), "DEPOSIT", request.getAmount(), true, "", BigDecimal.valueOf(newBal));
        return getVaultStatus();
    }

    @Transactional
    public VaultStatusResponse withdraw(VaultActionRequest request)
            throws InvalidAmountException, InsufficientBalanceException, InvalidPinException, VaultLockedException {

        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than 0.",
                    request.getAmount() != null ? request.getAmount().doubleValue() : 0.0);
        }

        EmergencyVaultEntity vault = getOrCreateVault();
        if (vault.getLockedUntil() != null && vault.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new VaultLockedException("Vault is temporarily locked due to previous failed attempts.",
                    vault.getLockedUntil().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        EmergencyVault coreVault = new EmergencyVault(vault.getReserveBalance().doubleValue(), defaultDemoPin);

        try {
            double newBal = coreVault.withdraw(request.getAmount().doubleValue(), request.getPin());
            vault.setReserveBalance(BigDecimal.valueOf(newBal));
            vault.setFailedAttempts(0);
            vault.setLockedUntil(null);
            vaultRepository.save(vault);

            recordAuditLog(vault.getVaultId(), "WITHDRAW_SUCCESS", request.getAmount(), true, "", BigDecimal.valueOf(newBal));
        } catch (InvalidPinException ipe) {
            int failed = (vault.getFailedAttempts() != null ? vault.getFailedAttempts() : 0) + 1;
            vault.setFailedAttempts(failed);
            if (failed >= EmergencyVault.MAX_FAILED_ATTEMPTS) {
                vault.setLockedUntil(LocalDateTime.now().plusSeconds(60));
                vault.setIsLocked(true);
            }
            vaultRepository.save(vault);
            recordAuditLog(vault.getVaultId(), "WITHDRAW_FAILED", request.getAmount(), false, "Invalid PIN", vault.getReserveBalance());
            throw ipe;
        } catch (InsufficientBalanceException | InvalidAmountException ex) {
            recordAuditLog(vault.getVaultId(), "WITHDRAW_FAILED", request.getAmount(), false, ex.getMessage(), vault.getReserveBalance());
            throw ex;
        }

        return getVaultStatus();
    }

    @Transactional
    public VaultStatusResponse unlock(String pin) throws InvalidPinException, VaultLockedException {
        EmergencyVaultEntity vault = getOrCreateVault();
        if (vault.getLockedUntil() != null && vault.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new VaultLockedException("Vault locked for security.",
                    vault.getLockedUntil().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        EmergencyVault coreVault = new EmergencyVault(vault.getReserveBalance().doubleValue(), defaultDemoPin);
        try {
            coreVault.unlock(pin);
            vault.setIsLocked(false);
            vault.setFailedAttempts(0);
            vault.setLockedUntil(null);
            vaultRepository.save(vault);
            recordAuditLog(vault.getVaultId(), "UNLOCK_SUCCESS", BigDecimal.ZERO, true, "", vault.getReserveBalance());
        } catch (InvalidPinException ipe) {
            int failed = (vault.getFailedAttempts() != null ? vault.getFailedAttempts() : 0) + 1;
            vault.setFailedAttempts(failed);
            if (failed >= EmergencyVault.MAX_FAILED_ATTEMPTS) {
                vault.setLockedUntil(LocalDateTime.now().plusSeconds(60));
                vault.setIsLocked(true);
            }
            vaultRepository.save(vault);
            recordAuditLog(vault.getVaultId(), "UNLOCK_FAILED", BigDecimal.ZERO, false, "Invalid PIN", vault.getReserveBalance());
            throw ipe;
        }

        return getVaultStatus();
    }

    private void recordAuditLog(Long vaultId, String action, BigDecimal amount, boolean success, String reason, BigDecimal resultingBalance) {
        VaultAuditLogEntity log = new VaultAuditLogEntity(vaultId, action, amount, success, reason, resultingBalance);
        auditLogRepository.save(log);
    }

    public EmergencyVaultEntity getOrCreateVault() {
        UserEntity user = paymentService.getOrCreateDemoUser();
        return vaultRepository.findByUserId(user.getUserId())
                .orElseGet(() -> vaultRepository.save(new EmergencyVaultEntity(
                        user.getUserId(),
                        BigDecimal.valueOf(15000.00),
                        false,
                        0
                )));
    }
}
