package com.bookapp.web.controller;

import com.bookapp.core.service.UserService;
import com.bookapp.web.dto.RegisterDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Невірне ім'я користувача або пароль");
        }

        if (logout != null) {
            model.addAttribute("message", "Ви успішно вийшли з системи");
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

            if (registerDto.getPassword() == null || registerDto.getPassword().length() < 4) {
                model.addAttribute("error", "Пароль має містити мінімум 4 символи");
                return "auth/register";
            }

            if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
                model.addAttribute("error", "Паролі не співпадають");
                return "auth/register";
            }

            // Хешування пароля та реєстрація
            String encodedPassword = passwordEncoder.encode(registerDto.getPassword());
            userService.registerUser(registerDto.getUsername(), encodedPassword);

            log.info("User registered successfully: {}", registerDto.getUsername());
            return "redirect:/login?registered=true";

        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", "Помилка реєстрації: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}