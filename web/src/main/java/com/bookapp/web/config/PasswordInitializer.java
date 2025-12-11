package com.bookapp.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Автоматичне хешування паролів при запуску застосунку
 * Перевіряє чи паролі вже захешовані, і якщо ні - хешує їх
 */
@Component
public class PasswordInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            // Перевіряємо чи паролі вже захешовані
            String adminPassword = jdbcTemplate.queryForObject(
                    "SELECT password FROM users WHERE username = 'admin'",
                    String.class
            );

            // Якщо пароль не починається з $2a$ (BCrypt), то він не захешований
            if (adminPassword != null && !adminPassword.startsWith("$2a$")) {
                System.out.println("🔒 Виявлено незахешовані паролі. Починаємо хешування...");

                // Хешуємо паролі
                String hashedAdmin = passwordEncoder.encode("admin123");
                String hashedUser = passwordEncoder.encode("password123");

                // Оновлюємо в БД
                jdbcTemplate.update(
                        "UPDATE users SET password = ? WHERE username = 'admin'",
                        hashedAdmin
                );

                jdbcTemplate.update(
                        "UPDATE users SET password = ? WHERE username IN ('ivan', 'maria', 'olena', 'dmytro')",
                        hashedUser
                );

                System.out.println("✅ Паролі успішно захешовані!");
                System.out.println("   admin: admin123");
                System.out.println("   ivan, maria, olena, dmytro: password123");
            } else {
                System.out.println("✅ Паролі вже захешовані");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Помилка при ініціалізації паролів: " + e.getMessage());
        }
    }
}