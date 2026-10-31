package com.bookmytree.platform.auth.controller;

import com.bookmytree.platform.auth.dto.*;
import com.bookmytree.platform.auth.entity.User;
import com.bookmytree.platform.auth.service.AuthService;
import com.bookmytree.platform.auth.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto response = authService.register(request);
        String token = jwtService.generateToken(response.getEmail(), response.getRole().name());
        return new ResponseEntity<>(new AuthResponseDto(token, response), HttpStatus.CREATED);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        UserResponseDto response = authService.login(request);
        String token = jwtService.generateToken(response.getEmail(), response.getRole().name());
        return ResponseEntity.ok(new AuthResponseDto(token, response));
    }

    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<UserResponseDto> getProfile(@PathVariable UUID userId) {
        UserResponseDto response = authService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/profile")
    public ResponseEntity<UserResponseDto> updateProfile(
            @PathVariable UUID userId,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String deliveryAddress) {
        UserResponseDto response = authService.updateProfile(userId, fullName, phoneNumber, deliveryAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/farmers/verify-request")
    public ResponseEntity<UserResponseDto> submitFarmerVerification(
            @Valid @RequestBody FarmerVerificationRequestDto request) {
        UserResponseDto response = authService.submitFarmerVerification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/manager/farmers/pending")
    public ResponseEntity<List<UserResponseDto>> getPendingFarmers() {
        List<UserResponseDto> pending = authService.getPendingFarmers();
        return ResponseEntity.ok(pending);
    }

    @PatchMapping("/manager/farmers/{farmerId}/status")
    public ResponseEntity<UserResponseDto> updateFarmerStatus(
            @PathVariable UUID farmerId,
            @RequestParam User.VerificationStatus status) {
        UserResponseDto response = authService.updateFarmerVerificationStatus(farmerId, status);
        return ResponseEntity.ok(response);
    }
}