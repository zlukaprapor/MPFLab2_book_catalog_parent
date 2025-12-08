package com.bookapp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.bookapp")
@EnableJpaRepositories(basePackages = "com.bookapp.persistence.repository")
@EntityScan(basePackages = "com.bookapp.persistence.entity")
public class BookCatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookCatalogApplication.class, args);
    }
}