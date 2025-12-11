package com.bookapp.web.controller;

import com.bookapp.core.service.UserService;
import com.bookapp.persistence.entity.UserEntity;
import com.bookapp.persistence.repository.UserRepository;
import com.bookapp.web.dto.RegisterDto;
import com.bookapp.web.service.EmailConfirmationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailConfirmationService confirmationService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "disabled", required = false) String disabled,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Невірне ім'я користувача або пароль");
        }

        if (logout != null) {
            model.addAttribute("message", "Ви успішно вийшли з системи");
        }

        if (disabled != null) {
            model.addAttribute("error", "Ваш акаунт не активовано. Перевірте email для підтвердження.");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") RegisterDto registerDto, Model model) {
        try {
            log.info("Attempting to register user: {}", registerDto.getUsername());

            // Валідація
            if (registerDto.getUsername() == null || registerDto.getUsername().trim().isEmpty()) {
                model.addAttribute("error", "Ім'я користувача є обов'язковим");
                return "auth/register";
            }

            if (registerDto.getEmail() == null || !registerDto.getEmail().contains("@")) {
                model.addAttribute("error", "Введіть коректну email адресу");
                return "auth/register";
            }

            if (registerDto.getPassword() == null || registerDto.getPassword().length() < 4) {
                model.addAttribute("error", "Пароль має містити мінімум 4 символи");
                return "auth/register";
            }

            if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
                model.addAttribute("error", "Паролі не співпадають");
                return "auth/register";
            }

            // Створення користувача
            UserEntity user = new UserEntity();
            user.setUsername(registerDto.getUsername());
            user.setEmail(registerDto.getEmail());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setRole("USER");
            user.setEnabled(false); // Спочатку неактивний

            userRepository.save(user);

            // Створення токена підтвердження та відправка email
            confirmationService.createConfirmationToken(user);

            log.info("User registered successfully: {}", registerDto.getUsername());
            return "redirect:/register?success=true";

        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", "Помилка реєстрації: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam("token") String token, Model model) {
        log.info("Email confirmation attempt with token: {}", token);

        boolean confirmed = confirmationService.confirmEmail(token);

        if (confirmed) {
            model.addAttribute("message", "Email успішно підтверджено! Тепер ви можете увійти.");
            return "auth/confirmation-success";
        } else {
            model.addAttribute("error", "Невірний або прострочений токен підтвердження.");
            return "auth/confirmation-error";
        }
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}