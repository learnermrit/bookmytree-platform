package com.bookmytree.platform.gatepass.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "visit_gate_passes", indexes = {
    @Index(name = "idx_pass_code", columnList = "passCode"),
    @Index(name = "idx_user_id", columnList = "userId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VisitGatePass extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId; // Which orchard they are visiting

    @Column(name = "pass_code", unique = true, nullable = false)
    private String passCode; // Cryptographic secure token / QR content

    @Column(name = "otp_code", length = 6, nullable = false)
    private String otpCode; // 6-digit numeric verification code

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private boolean isUsed = false;
}