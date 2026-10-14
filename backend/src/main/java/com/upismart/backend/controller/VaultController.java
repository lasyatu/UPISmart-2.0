package com.upismart.backend.controller;

import com.upismart.backend.core.exception.InsufficientBalanceException;
import com.upismart.backend.core.exception.InvalidAmountException;
import com.upismart.backend.core.exception.InvalidPinException;
import com.upismart.backend.core.exception.VaultLockedException;
import com.upismart.backend.dto.VaultActionRequest;
import com.upismart.backend.dto.VaultStatusResponse;
import com.upismart.backend.service.VaultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vault")
@CrossOrigin(origins = "*")
public class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping
    public ResponseEntity<VaultStatusResponse> getVaultStatus() {
        return ResponseEntity.ok(vaultService.getVaultStatus());
    }

    @PostMapping("/deposit")
    public ResponseEntity<VaultStatusResponse> deposit(@RequestBody VaultActionRequest request)
            throws InvalidAmountException {
        return ResponseEntity.ok(vaultService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<VaultStatusResponse> withdraw(@RequestBody VaultActionRequest request)
            throws InvalidAmountException, InsufficientBalanceException, InvalidPinException, VaultLockedException {
        return ResponseEntity.ok(vaultService.withdraw(request));
    }

    @PostMapping("/unlock")
    public ResponseEntity<VaultStatusResponse> unlock(@RequestBody VaultActionRequest request)
            throws InvalidPinException, VaultLockedException {
        return ResponseEntity.ok(vaultService.unlock(request.getPin()));
    }
}
