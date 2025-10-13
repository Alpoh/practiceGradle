package co.medina.starter.practice.auth;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String verificationLink);
}