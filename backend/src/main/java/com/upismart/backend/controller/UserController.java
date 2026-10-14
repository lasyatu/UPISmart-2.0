package com.upismart.backend.controller;

import com.upismart.backend.dto.UserProfileResponse;
import com.upismart.backend.entity.UserEntity;
import com.upismart.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final PaymentService paymentService;

    public UserController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        UserEntity user = paymentService.getOrCreateDemoUser();
        UserProfileResponse response = new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getUpiHandle(),
                user.getMonthlySalary(),
                Boolean.TRUE.equals(user.getBiometricsEnabled())
        );
        return ResponseEntity.ok(response);
    }
}
