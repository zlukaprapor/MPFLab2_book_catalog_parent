# Book Catalog - Трирівнева веб-архітектура

Багатомодульний Maven-проєкт з чіткою трирівневою архітектурою для каталогу книг з відгуками.

## Структура проєкту

```
book-catalog-parent/
├── core/          # Доменна логіка та порти (jar)
├── persistence/   # Реалізація доступу до даних JDBC/H2 (jar)
└── web/           # HTTP-інтерфейс Servlet API (war)
```

## Модулі

### Core (доменна логіка)
- **Моделі**: `Book`, `Comment`, `Page`, `PageRequest`
- **Порти**: `CatalogRepositoryPort`, `CommentRepositoryPort`
- **Сервіси**: `CatalogService`, `CommentService` з бізнес-валідацією
- **Правила**: видалення коментаря тільки протягом 24 годин

### Persistence (дані)
- **Реалізації портів**: `CatalogRepository`, `CommentRepository`
- **База даних**: H2 in-memory
- **Схема**: автоматичне створення + тестові дані

### Web (HTTP)
- **Контролер**: `BookController` (servlet)
- **Endpoints**:
    - `GET /books?q=query&page=0&size=10&sort=title` - список книг
    - `GET /books/{id}` - деталі книги з коментарями
    - `POST /books/{id}/comments` - додати коментар
    - `DELETE /books/{id}/comments/{commentId}` - видалити коментар
- **Формат відповідей**: JSON (UTF-8)
- **Коди статусу**: 200, 201, 204, 400, 404, 409, 500

## Запуск

### Попередні вимоги
- Java 11+
- Maven 3.6+

### Збірка
```bash
mvn clean install
```

### Запуск на Jetty 11
```bash
cd web
mvn jetty:run
```

Додаток буде доступний за адресою: `http://localhost:8080`

## Приклади використання

### Отримати список книг
```bash
curl "http://localhost:8080/books?q=java&page=0&size=5&sort=year"
```

### Отримати книгу
```bash
curl "http://localhost:8080/books/1"
```

### Додати коментар
```bash
curl -X POST "http://localhost:8080/books/1/comments" \
  -H "Content-Type: application/json" \
  -d '{"author":"Іван","text":"Чудова книга!"}'
```

### Видалити коментар (тільки протягом 24 годин)
```bash
curl -X DELETE "http://localhost:8080/books/1/comments/1"
```

## Логування (SLF4J/Logback)

- **INFO**: створення/видалення коментарів з ключовими полями
- **WARN**: помилки клієнта (4xx)
- **ERROR**: помилки сервера (5xx)

Приклад логів:
```
INFO  c.b.c.service.CommentService - Comment created: id=1, bookId=1, author=Іван
INFO  c.b.c.service.CommentService - Comment deleted: id=1, bookId=1
WARN  c.b.web.controller.BookController - Client error 400: Author is required
```

## ArchUnit тести

Автоматична перевірка архітектурних правил:

1. ✅ `web` не залежить від `persistence`
2. ✅ `core` не залежить від Servlet API
3. ✅ `core` не залежить від JDBC
4. ✅ Контролери тільки у `web`
5. ✅ Репозиторії тільки у `persistence`
6. ✅ Шаровість: Web → Core ← Persistence

### Запуск тестів
```bash
cd web
mvn test
```

## Технології

- **Java 11**
- **Maven** (багатомодульний проєкт)
- **Servlet API 4.0** (HTTP)
- **H2 Database** (in-memory)
- **JDBC** (доступ до даних)
- **Gson** (JSON)
- **SLF4J + Logback** (логування)
- **JUnit 5** (тести)
- **ArchUnit** (архітектурні тести)
- **Jetty 11** (сервер)

## Функціонал

### Пошук книг
- Пошук за назвою або автором
- Регістронезалежний пошук
- Підтримка кирилиці

### Пагінація
- Параметри: `page` (з 0), `size` (1-100)
- Метадані: totalElements, totalPages, hasNext, hasPrevious

### Сортування
- За назвою (`sort=title`)
- За автором (`sort=author`)
- За роком (`sort=year`)

### Валідація коментарів
- Автор: обов'язковий, max 100 символів
- Текст: обов'язковий, max 1000 символів
- Видалення: тільки протягом 24 годин після створення

## Формат помилок

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Author is required"
}
```

## Архітектура

Проєкт реалізує **Hexagonal Architecture** (Ports & Adapters):

- **Core** - незалежне ядро з бізнес-логікою
- **Ports** - інтерфейси для зовнішнього світу
- **Adapters** - реалізації в persistence та web

Залежності йдуть **від зовнішніх шарів до центру**:
```
Web (HTTP) ──┐
             ├──> Core (Domain + Ports)
Persistence ─┘
```