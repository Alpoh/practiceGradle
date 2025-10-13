package co.medina.starter.practice.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "uk_users_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "mobile_number", length = 32)
    private String mobileNumber;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String address;

    // added password for authentication (nullable for existing users)
    @Column(length = 255)
    private String password;

    // email verification flow
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "verification_token", length = 64)
    private String verificationToken;

    @Column(name = "verification_expires_at", length = 40)
    private String verificationExpiresAt; // ISO-8601 instant as string to keep it simple
}
