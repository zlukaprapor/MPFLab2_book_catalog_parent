package com.bookapp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Головний клас Spring Boot застосунку
 *
 * @SpringBootApplication включає:
 * - @Configuration: дозволяє визначати біни
 * - @EnableAutoConfiguration: автоматична конфігурація Spring
 * - @ComponentScan: сканування компонентів у пакеті та підпакетах
 */
@SpringBootApplication(scanBasePackages = "com.bookapp")
public class BookCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookCatalogApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("📚 Застосунок запущено!");
        System.out.println("🌐 Головна сторінка: http://localhost:8080/");
        System.out.println("📖 Каталог книг: http://localhost:8080/books");
        System.out.println("===========================================\n");
    }
}