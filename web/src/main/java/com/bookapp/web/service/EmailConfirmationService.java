package com.bookapp.web.service;

import com.bookapp.persistence.entity.ConfirmationTokenEntity;
import com.bookapp.persistence.entity.UserEntity;
import com.bookapp.persistence.repository.ConfirmationTokenRepository;
import com.bookapp.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailConfirmationService {

    private static final Logger log = LoggerFactory.getLogger(EmailConfirmationService.class);
    private static final int EXPIRATION_HOURS = 24;

    @Autowired
    private ConfirmationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    /**
     * Створення токена підтвердження та відправка email
     */
    @Transactional
    public String createConfirmationToken(UserEntity user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(EXPIRATION_HOURS);

        ConfirmationTokenEntity confirmationToken = new ConfirmationTokenEntity(
                token,
                user,
                expiresAt
        );

        tokenRepository.save(confirmationToken);
        log.info("Created confirmation token for user: {}", user.getUsername());

        // Відправка email
        try {
            mailService.sendConfirmationEmail(user.getEmail(), user.getUsername(), token);
            log.info("Confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send confirmation email", e);
        }

        return token;
    }

    /**
     * Підтвердження email за токеном
     */
    @Transactional
    public boolean confirmEmail(String token) {
        Optional<ConfirmationTokenEntity> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            log.warn("Confirmation token not found: {}", token);
            return false;
        }

        ConfirmationTokenEntity confirmationToken = tokenOpt.get();

        // Перевірка чи токен вже використаний
        if (confirmationToken.getConfirmedAt() != null) {
            log.warn("Token already confirmed: {}", token);
            return false;
        }

        // Перевірка чи токен не прострочений
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Token expired: {}", token);
            return false;
        }

        // Підтвердження
        confirmationToken.setConfirmedAt(LocalDateTime.now());

        UserEntity user = confirmationToken.getUser();
        user.setEnabled(true);

        userRepository.save(user);
        tokenRepository.save(confirmationToken);

        log.info("Email confirmed for user: {}", user.getUsername());
        return true;
    }

    /**
     * Перевірка чи email підтверджений
     */
    public boolean isEmailConfirmed(String username) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        return userOpt.map(UserEntity::getEnabled).orElse(false);
    }
}