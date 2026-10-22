package com.bookmytree.platform.gatepass.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyPassRequestDto {
    @NotBlank(message = "Pass code is required")
    private String passCode;

    @NotBlank(message = "OTP code is required")
    private String otpCode;
}