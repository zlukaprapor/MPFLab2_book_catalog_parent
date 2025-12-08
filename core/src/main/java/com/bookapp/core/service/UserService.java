package com.bookapp.core.service;

import com.bookapp.core.domain.User;
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
        log.debug("Getting user by id={}", id);
        return repository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        log.debug("Getting user by username={}", username);
        return repository.findByUsername(username).orElse(null);
    }
}