package com.upismart.backend.controller;

import com.upismart.backend.core.exception.InvalidAmountException;
import com.upismart.backend.dto.SalaryUpdateRequest;
import com.upismart.backend.dto.SalaryUtilizationResponse;
import com.upismart.backend.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
@CrossOrigin(origins = "*")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/utilization")
    public ResponseEntity<SalaryUtilizationResponse> getSalaryUtilization() {
        return ResponseEntity.ok(budgetService.getSalaryUtilization());
    }

    @PutMapping("/salary")
    public ResponseEntity<SalaryUtilizationResponse> updateSalary(@Valid @RequestBody SalaryUpdateRequest request)
            throws InvalidAmountException {
        return ResponseEntity.ok(budgetService.updateMonthlySalary(request));
    }
}
