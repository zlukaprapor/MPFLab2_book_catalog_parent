# Міграція проєкту Book Catalog на Spring Boot

## Огляд змін

### Що було:
- Ручна ініціалізація об'єктів через `ApplicationInitializer`
- Сервлети з `@WebServlet` анотаціями
- Jetty як зовнішній сервер
- Статичне з'єднання між шарами

### Що стало:
- Spring Boot з автоконфігурацією
- IoC контейнер керує життєвим циклом бінів
- REST API контролери замість сервлетів
- Вбудований Tomcat
- Ін'єкція залежностей через конструктори та поля



---

## Структура проєкту

```
book-catalog-parent/
├── pom.xml                          # Parent POM з Spring Boot
├── core/
│   └── service/
│       ├── CatalogService.java      # @Service
│       └── CommentService.java      # @Service + @Value
├── persistence/
│   ├── CatalogRepository.java       # @Repository
│   └── CommentRepository.java       # @Repository
├── infrastructure/
│   └── ApplicationConfig.java       # @Configuration + @Bean
└── web/
    ├── pom.xml                      # Spring Boot dependencies
    ├── BookCatalogApplication.java  # @SpringBootApplication
    ├── controller/
    │   ├── BookRestController.java  # @RestController (API)
    │   └── WebController.java       # @Controller (HTML)
    └── resources/
        ├── application.properties   # Конфігурація
        ├── application.yml          # Альтернативна конфігурація
        └── banner.txt               # Кастомний банер
```

