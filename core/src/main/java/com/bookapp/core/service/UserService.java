package com.bookapp.core.service;

import com.bookapp.core.domain.User;
import com.bookapp.core.exception.ValidationException;
import com.bookapp.core.port.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepositoryPort repository;

    public UserService(UserRepositoryPort repository) {
        this.repository = repository;
        log.info("UserService initialized");
    }

    public User findById(Long id) {
        log.debug("Finding user by id={}", id);
        return repository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        log.debug("Finding user by username={}", username);
        return repository.findByUsername(username).orElse(null);
    }

    public User registerUser(String username, String encodedPassword) {
        log.debug("Registering new user: username={}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Ім'я користувача є обов'язковим");
        }

        if (repository.existsByUsername(username)) {
            throw new ValidationException("Користувач з таким ім'ям вже існує");
        }

        User newUser = new User(null, username, encodedPassword, "USER");
        User saved = repository.save(newUser);
        log.info("User registered: id={}, username={}", saved.getId(), saved.getUsername());

        return saved;
    }
}