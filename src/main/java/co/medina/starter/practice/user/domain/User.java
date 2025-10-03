package co.medina.starter.practice.user.domain;

import jakarta.persistence.*;
import lombok.*;

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
}
