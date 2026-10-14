package com.upismart.backend.controller;

import com.upismart.backend.core.exception.*;
import com.upismart.backend.dto.PaymentApiRequest;
import com.upismart.backend.dto.PaymentApiResponse;
import com.upismart.backend.entity.TransactionEntity;
import com.upismart.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final PaymentService paymentService;

    public TransactionController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionEntity>> getAllTransactions() {
        return ResponseEntity.ok(paymentService.getAllTransactions());
    }

    @PostMapping
    public ResponseEntity<PaymentApiResponse> processPayment(@Valid @RequestBody PaymentApiRequest request)
            throws InvalidAmountException,
                   InsufficientBalanceException,
                   InvalidPinException,
                   InvalidPaymentDetailsException,
                   UnusualSpendingAlertException {

        PaymentApiResponse response = paymentService.executePayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
