package com.bookapp.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Публічні ресурси
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // H2 Console (тільки для розробки!)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Сторінки авторизації та реєстрації
                        .requestMatchers("/login", "/register", "/auth/**").permitAll()

                        // Перегляд книг - для USER і ADMIN
                        .requestMatchers(HttpMethod.GET, "/books", "/books/{id}").hasAnyRole("USER", "ADMIN")

                        // Додавання книг - тільки ADMIN
                        .requestMatchers("/books/add", "/books/*/edit", "/books/*/delete").hasRole("ADMIN")

                        // Коментарі - додавання для USER і ADMIN, видалення тільки ADMIN
                        .requestMatchers(HttpMethod.POST, "/books/*/comments").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/books/*/comments/*/delete").hasRole("ADMIN")

                        // API - тільки для авторизованих
                        .requestMatchers("/api/**").authenticated()

                        // Решта запитів - авторизація обов'язкова
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/books", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                );

        // Вимкнення CSRF та Frame Options для H2 Console (тільки для розробки!)
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
        );
        http.headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
        );

        return http.build();
    }
}