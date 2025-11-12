package com.bookapp.web.config;

import com.bookapp.core.service.CatalogService;
import com.bookapp.core.service.CommentService;
import com.bookapp.infrastructure.ApplicationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public CatalogService catalogService() {
        return ApplicationInitializer.createCatalogService();
    }

    @Bean
    public CommentService commentService() {
        return ApplicationInitializer.createCommentService();
    }
}
