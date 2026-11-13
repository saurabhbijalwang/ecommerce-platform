package com.ecommerce.platform.cms.controller;

import com.ecommerce.common.api.ApiResponse;
import com.ecommerce.platform.cms.exception.CatalogConflictException;
import com.ecommerce.platform.cms.exception.CatalogNotFoundException;
import com.ecommerce.platform.cms.exception.InvalidCatalogRequestException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class CmsExceptionHandler {

    @ExceptionHandler(CatalogNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(CatalogNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, "Catalog resource not found", List.of(exception.getMessage()));
    }

    @ExceptionHandler({CatalogConflictException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException exception) {
        String message = exception instanceof CatalogConflictException
            ? exception.getMessage()
            : "The catalog resource changed concurrently. Reload it and retry.";
        return response(HttpStatus.CONFLICT, "Catalog conflict", List.of(message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(DataIntegrityViolationException exception) {
        return response(
            HttpStatus.CONFLICT,
            "Catalog constraint violation",
            List.of("A catalog constraint prevented this operation.")
        );
    }

    @ExceptionHandler(InvalidCatalogRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(InvalidCatalogRequestException exception) {
        return response(HttpStatus.BAD_REQUEST, "Invalid catalog request", List.of(exception.getMessage()));
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
