package com.bookmytree.platform.gatepass.service;

import com.bookmytree.platform.common.exception.BadRequestException;
import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.gatepass.dto.GatePassResponseDto;
import com.bookmytree.platform.gatepass.entity.VisitGatePass;
import com.bookmytree.platform.gatepass.repository.GatePassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatePassService {

    private final GatePassRepository gatePassRepository;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public GatePassResponseDto generateGatePass(UUID userId, UUID fieldId) {
        log.info("Generating visit gate pass for user: {} visiting field: {}", userId, fieldId);

        String passCode = "PASS-" + UUID.randomUUID().toString().toUpperCase();
        String otpCode = String.format("%06d", secureRandom.nextInt(1000000));

        VisitGatePass gatePass = new VisitGatePass();
        gatePass.setUserId(userId);
        gatePass.setFieldId(fieldId);
        gatePass.setPassCode(passCode);
        gatePass.setOtpCode(otpCode);
        gatePass.setValidUntil(LocalDateTime.now().plusHours(24));
        gatePass.setUsed(false);

        VisitGatePass savedPass = gatePassRepository.save(gatePass);
        log.info("Gate pass generated successfully with code: {}", passCode);

        return mapToDto(savedPass);
    }

    @Transactional(readOnly = true)
    public List<GatePassResponseDto> getPassesByUser(UUID userId) {
        return gatePassRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean verifyAndRedeemPass(String passCode, String otpCode) {
         log.info("Verifying gate pass code at orchard entrance: {}", passCode);

        VisitGatePass gatePass = gatePassRepository.findByPassCode(passCode)
                .orElseThrow(() -> new ResourceNotFoundException("Gate pass not found: " + passCode));

        if (gatePass.isUsed()) {
            throw new BadRequestException("This gate pass has already been used.");
        }

        if (gatePass.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This gate pass has expired.");
        }

        if (!gatePass.getOtpCode().equals(otpCode)) {
            throw new BadRequestException("Invalid 6-digit OTP verification code.");
        }

        gatePass.setUsed(true);
        gatePassRepository.save(gatePass);
        log.info("Gate pass successfully verified and redeemed for pass code: {}", passCode);

        return true;
    }

    private GatePassResponseDto mapToDto(VisitGatePass pass) {
        return GatePassResponseDto.builder()
                .id(pass.getId())
                .userId(pass.getUserId())
                .fieldId(pass.getFieldId())
                .passCode(pass.getPassCode())
                .otpCode(pass.getOtpCode())
                .validUntil(pass.getValidUntil())
                .isUsed(pass.isUsed())
                .build();
    }
}