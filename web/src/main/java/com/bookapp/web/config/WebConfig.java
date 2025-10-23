package com.bookapp.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Конфігурація Spring MVC
 *
 * @EnableWebMvc - активує Spring MVC і налаштовує:
 * - DispatcherServlet як Front Controller
 * - HandlerMapping для маршрутизації запитів
 * - HandlerAdapter для виклику методів контролерів
 * - ViewResolver для рендерингу відповідей
 * - HttpMessageConverter для JSON серіалізації
 *
 * WebMvcConfigurer - дозволяє кастомізувати конфігурацію MVC
 */
@Configuration
//@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    public WebConfig() {
        log.info("Initializing Spring MVC configuration with @EnableWebMvc");
    }

    /**
     * Налаштування обробки статичних ресурсів (HTML, CSS, JS)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.debug("Configuring static resource handlers");
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * Налаштування CORS для доступу до API з різних доменів
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.debug("Configuring CORS mappings");
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
