package com.bookapp.web.config;

import com.bookapp.core.port.CatalogRepositoryPort;
import com.bookapp.core.port.CommentRepositoryPort;
import com.bookapp.core.port.UserRepositoryPort;
import com.bookapp.core.service.CatalogService;
import com.bookapp.core.service.CommentService;
import com.bookapp.core.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public CatalogService catalogService(CatalogRepositoryPort catalogRepository) {
        return new CatalogService(catalogRepository);
    }

    @Bean
    public CommentService commentService(CommentRepositoryPort commentRepository) {
        return new CommentService(commentRepository);
    }

    @Bean
    public UserService userService(UserRepositoryPort userRepository) {
        return new UserService(userRepository);
    }
}