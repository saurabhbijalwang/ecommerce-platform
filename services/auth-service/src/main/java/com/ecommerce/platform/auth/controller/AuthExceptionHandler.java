package com.ecommerce.platform.auth.controller;

import com.ecommerce.common.api.ApiResponse;
import com.ecommerce.platform.auth.exception.AuthConflictException;
import com.ecommerce.platform.auth.exception.AuthRequestException;
import com.ecommerce.platform.auth.exception.InvalidCredentialsException;
import com.ecommerce.platform.auth.exception.InvalidRefreshTokenException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(RuntimeException exception) {
        return response(HttpStatus.UNAUTHORIZED, "Authentication failed", List.of(exception.getMessage()));
    }

    @ExceptionHandler(AuthConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(AuthConflictException exception) {
        return response(HttpStatus.CONFLICT, "Account conflict", List.of(exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(DataIntegrityViolationException exception) {
        return response(
            HttpStatus.CONFLICT,
            "Account constraint violation",
            List.of("An account constraint prevented this operation.")
        );
    }

    @ExceptionHandler(AuthRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(AuthRequestException exception) {
        return response(HttpStatus.BAD_REQUEST, "Invalid authentication request", List.of(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .toList();
        return response(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return response(
            HttpStatus.BAD_REQUEST,
            "Invalid request parameter",
            List.of("Invalid value for " + exception.getName() + ".")
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return response(
            HttpStatus.BAD_REQUEST,
            "Invalid request body",
            List.of("The request body is malformed or contains an unsupported value.")
        );
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ApiResponse<Void>> response(
        HttpStatus status,
        String message,
        List<String> errors
    ) {
        return ResponseEntity.status(status).body(ApiResponse.<Void>failure(message, errors));
    }
}
