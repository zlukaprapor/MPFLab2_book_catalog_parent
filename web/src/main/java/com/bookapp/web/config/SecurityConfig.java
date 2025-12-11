package com.bookapp.web.config;

import com.bookapp.web.security.CustomLogoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomLogoutHandler customLogoutHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Статичні ресурси доступні всім
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Сторінки авторизації та реєстрації доступні всім
                        .requestMatchers("/login", "/register", "/auth/**").permitAll()
                        .requestMatchers("/confirm").permitAll()

                        // Доступ для ADMIN до управління книгами
                        .requestMatchers("/books/add", "/books/*/edit", "/books/*/delete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/books/*/comments/*/delete").hasRole("ADMIN")

                        // Доступ для USER та ADMIN до перегляду книг та додавання коментарів
                        .requestMatchers(HttpMethod.GET, "/books", "/books/{id}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/books/*/comments").hasAnyRole("USER", "ADMIN")

                        // Всі API-запити потребують авторизації
                        .requestMatchers("/api/**").authenticated()

                        // Все інше потребує авторизації
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
                        .addLogoutHandler(customLogoutHandler) // наш кастомний handler
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                );

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
