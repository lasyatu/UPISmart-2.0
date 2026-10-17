package com.upismart.backend.controller;

import com.upismart.backend.dto.BillRequest;
import com.upismart.backend.dto.BillResponse;
import com.upismart.backend.service.BillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public ResponseEntity<List<BillResponse>> getBills() {
        return ResponseEntity.ok(billService.getBillsForUser());
    }

    @PostMapping
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest request) {
        BillResponse created = billService.createBill(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<BillResponse> payBill(@PathVariable("id") Long id) {
        return ResponseEntity.ok(billService.payBill(id));
    }
}
