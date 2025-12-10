package com.bookapp.core.port;

import com.bookapp.core.domain.User;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    User save(User user);
    boolean existsByUsername(String username);
}