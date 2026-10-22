package com.bookmytree.platform.gatepass.controller;

import com.bookmytree.platform.gatepass.entity.VisitGatePass;
import com.bookmytree.platform.gatepass.service.GatePassService;
import lombok.Data;
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
    public ResponseEntity<VisitGatePass> generatePass(@RequestBody GeneratePassRequest request) {
        VisitGatePass pass = gatePassService.generateGatePass(request.getUserId(), request.getFieldId());
        return new ResponseEntity<>(pass, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VisitGatePass>> getUserPasses(@PathVariable UUID userId) {
        List<VisitGatePass> passes = gatePassService.getPassesByUser(userId);
        return ResponseEntity.ok(passes);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyPass(@RequestBody VerifyPassRequest request) {
        boolean verified = gatePassService.verifyAndRedeemPass(request.getPassCode(), request.getOtpCode());
        if (verified) {
            return ResponseEntity.ok("Gate pass verified successfully. Welcome to the orchard!");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification failed.");
    }

    @Data
    public static class GeneratePassRequest {
        private UUID userId;
        private UUID fieldId;
    }

    @Data
    public static class VerifyPassRequest {
        private String passCode;
        private String otpCode;
    }
}