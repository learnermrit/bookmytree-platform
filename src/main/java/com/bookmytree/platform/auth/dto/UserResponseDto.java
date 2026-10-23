package com.bookmytree.platform.auth.dto;

import com.bookmytree.platform.auth.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponseDto {
    private UUID id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String deliveryAddress;
    private User.Role role;
    private User.VerificationStatus verificationStatus;
}