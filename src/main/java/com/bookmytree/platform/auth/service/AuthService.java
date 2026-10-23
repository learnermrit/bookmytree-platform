package com.bookmytree.platform.auth.service;

import com.bookmytree.platform.auth.dto.*;
import com.bookmytree.platform.auth.entity.User;
import com.bookmytree.platform.auth.repository.UserRepository;
import com.bookmytree.platform.common.exception.BadRequestException;
import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already in use: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        // In a production app, encode password using BCrypt here
        user.setPassword(request.getPassword()); 
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());

        if (request.getRole() == User.Role.FARMER) {
            user.setVerificationStatus(User.VerificationStatus.PENDING);
        } else {
            user.setVerificationStatus(User.VerificationStatus.NOT_APPLICABLE);
        }

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return mapToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public String login(LoginRequestDto request) {
        log.info("Authenticating user: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Invalid password.");
        }

        // Return a mock signed JWT token or session handle for bootstrap
        return "mock-jwt-token-bearer-" + user.getId();
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return mapToDto(user);
    }

    @Transactional
    public UserResponseDto updateProfile(UUID userId, String fullName, String phoneNumber, String deliveryAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (fullName != null) user.setFullName(fullName);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
        if (deliveryAddress != null) user.setDeliveryAddress(deliveryAddress);

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public UserResponseDto submitFarmerVerification(FarmerVerificationRequestDto request) {
        User user = userRepository.findById(request.getFarmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + request.getFarmerId()));

        if (user.getRole() != User.Role.FARMER) {
            throw new BadRequestException("User is not registered as a farmer.");
        }

        user.setGovernmentIdInfo(request.getGovernmentIdInfo());
        user.setBankDetailsInfo(request.getBankDetailsInfo());
        user.setVerificationStatus(User.VerificationStatus.PENDING);

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getPendingFarmers() {
        return userRepository.findByVerificationStatus(User.VerificationStatus.PENDING).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateFarmerVerificationStatus(UUID farmerId, User.VerificationStatus status) {
        User user = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found with ID: " + farmerId));

        user.setVerificationStatus(status);
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    private UserResponseDto mapToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .deliveryAddress(user.getDeliveryAddress())
                .role(user.getRole())
                .verificationStatus(user.getVerificationStatus())
                .build();
    }
}