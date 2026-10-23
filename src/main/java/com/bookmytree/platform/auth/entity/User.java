package com.bookmytree.platform.auth.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Hashed password

    @Column(nullable = false)
    private String fullName;

    private String phoneNumber;

    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CONSUMER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.NOT_APPLICABLE;

    private String governmentIdInfo; // Encrypted or reference info for farmers

    private String bankDetailsInfo; // Bank routing/account details for farmers

    public enum Role {
        CONSUMER, FARMER, REGIONAL_MANAGER, ADMIN
    }

    public enum VerificationStatus {
        NOT_APPLICABLE, PENDING, APPROVED, REJECTED
    }
}