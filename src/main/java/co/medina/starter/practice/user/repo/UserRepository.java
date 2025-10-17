package co.medina.starter.practice.user.repo;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

import co.medina.starter.practice.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByVerificationToken(String verificationToken);
}
