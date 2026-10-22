package com.bookmytree.platform.gatepass.controller;

import com.bookmytree.platform.gatepass.dto.GeneratePassRequestDto;
import com.bookmytree.platform.gatepass.dto.GatePassResponseDto;
import com.bookmytree.platform.gatepass.dto.VerifyPassRequestDto;
import com.bookmytree.platform.gatepass.service.GatePassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gatepasses")
@RequiredArgsConstructor
public class GatePassController {

    private final GatePassService gatePassService;

    @PostMapping("/generate")
    public ResponseEntity<GatePassResponseDto> generatePass(@Valid @RequestBody GeneratePassRequestDto request) {
        GatePassResponseDto pass = gatePassService.generateGatePass(request.getUserId(), request.getFieldId());
        return new ResponseEntity<>(pass, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GatePassResponseDto>> getUserPasses(@PathVariable UUID userId) {
        List<GatePassResponseDto> passes = gatePassService.getPassesByUser(userId);
        return ResponseEntity.ok(passes);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPass(@Valid @RequestBody VerifyPassRequestDto request) {
        boolean verified = gatePassService.verifyAndRedeemPass(request.getPassCode(), request.getOtpCode());
        if (verified) {
            return ResponseEntity.ok("Gate pass verified successfully. Welcome to the orchard!");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification failed.");
    }
}