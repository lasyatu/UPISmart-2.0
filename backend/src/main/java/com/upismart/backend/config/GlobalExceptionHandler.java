package com.upismart.backend.config;

import com.upismart.backend.core.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAmount(InvalidAmountException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), Map.of("attemptedAmount", ex.getAttemptedAmount()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage(),
                Map.of("attemptedAmount", ex.getAttemptedAmount(), "availableBalance", ex.getAvailableBalance()));
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPin(InvalidPinException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getErrorCode(), ex.getMessage(),
                Map.of("remainingAttempts", ex.getRemainingAttempts()));
    }

    @ExceptionHandler(VaultLockedException.class)
    public ResponseEntity<Map<String, Object>> handleVaultLocked(VaultLockedException ex) {
        return buildErrorResponse(HttpStatus.LOCKED, ex.getErrorCode(), ex.getMessage(),
                Map.of("unlockTimestampEpochMs", ex.getUnlockTimestampEpochMs()));
    }

    @ExceptionHandler(InvalidPaymentDetailsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPaymentDetails(InvalidPaymentDetailsException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(),
                Map.of("invalidField", ex.getInvalidField() != null ? ex.getInvalidField() : ""));
    }

    @ExceptionHandler(UnusualSpendingAlertException.class)
    public ResponseEntity<Map<String, Object>> handleUnusualSpending(UnusualSpendingAlertException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(),
                Map.of("requiresConfirmation", true,
                       "attemptedAmount", ex.getAttemptedAmount(),
                       "thresholdAmount", ex.getThresholdAmount(),
                       "reason", ex.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Invalid request body fields.", fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", ex.getMessage(), Map.of());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String errorCode, String message, Object details) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("errorCode", errorCode);
        body.put("message", message);
        body.put("details", details);
        return new ResponseEntity<>(body, status);
    }
}
