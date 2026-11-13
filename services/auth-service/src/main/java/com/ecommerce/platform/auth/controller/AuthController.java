package com.ecommerce.platform.auth.controller;

import com.ecommerce.common.api.ApiResponse;
import com.ecommerce.platform.auth.dto.CreateUserRequest;
import com.ecommerce.platform.auth.dto.LoginRequest;
import com.ecommerce.platform.auth.dto.RefreshTokenRequest;
import com.ecommerce.platform.auth.dto.RegisterRequest;
import com.ecommerce.platform.auth.dto.TokenResponse;
import com.ecommerce.platform.auth.dto.UserResponse;
import com.ecommerce.platform.auth.service.AuthService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
        TokenResponse response = authService.register(request);
        return ResponseEntity.created(URI.create("/api/v1/auth/me"))
            .body(ApiResponse.success("Account created", response));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Tokens refreshed", authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.success("Logged out", null);
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse response = authService.createUser(request);
        return ResponseEntity.created(URI.create("/api/v1/auth/users/" + response.id()))
            .body(ApiResponse.success("User created", response));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        return ApiResponse.success(authService.getCurrentUser(authentication.getName()));
    }
}
