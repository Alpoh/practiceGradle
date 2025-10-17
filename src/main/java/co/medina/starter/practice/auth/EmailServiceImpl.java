package co.medina.starter.practice.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender; // may be null in tests or if mail not configured

    public EmailServiceImpl(@org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Override
    public void sendVerificationEmail(@NonNull String toEmail, @NonNull String verificationLink) {
        try {
            if (mailSender == null || fromAddress == null || fromAddress.isBlank()) {
                // Fallback to logging if mail sender not available or from not configured
                log.info("[EmailService] Verification email (no-op) to={} link={}", toEmail, verificationLink);
                return;
            }
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmail);
            msg.setSubject("Confirm your account");
            msg.setText("Bienvenido! Para activar tu cuenta, haz clic en el siguiente enlace:\n" + verificationLink + "\nSi no solicitaste esta cuenta, puedes ignorar este mensaje.");
            mailSender.send(msg);
            log.info("[EmailService] Verification email sent to {}", toEmail);
        } catch (Exception ex) {
            // Do not fail registration if email fails; just log the issue
            log.warn("[EmailService] Failed to send email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
