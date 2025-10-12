package com.bookapp.infrastructure;

import com.bookapp.persistence.DatabaseConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Конфігураційний клас Spring
 *
 * @Configuration - позначає клас як джерело визначення бінів
 * Методи з @Bean створюють та налаштовують об'єкти, керовані Spring
 */
@Configuration
public class ApplicationConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    @Value("${spring.application.name:Book Catalog}")
    private String appName;

    @Value("${app.version:1.0-SNAPSHOT}")
    private String appVersion;

    /**
     * Метод ініціалізації, викликається після створення бінів
     * @PostConstruct - виконується автоматично після ін'єкції залежностей
     */
    @PostConstruct
    public void init() {
        log.info("===========================================");
        log.info("Initializing application: {} v{}", appName, appVersion);
        log.info("===========================================");

        // Ініціалізація схеми БД
        DatabaseConnection.initSchema();
        log.info("Database schema initialized successfully");
    }

    /**
     * Кастомний бін для серіалізації/десеріалізації JSON
     *
     * @Bean - реєструє повернений об'єкт як Spring бін
     * Spring буде автоматично ін'єктити цей Gson всюди, де потрібно
     */
    @Bean
    public Gson gson() {
        log.info("Creating custom Gson bean with LocalDateTime adapter");

        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                                context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .setPrettyPrinting()
                .create();
    }

    /**
     * Бін для форматування дат
     */
    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        log.info("Creating DateTimeFormatter bean");
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    }
}