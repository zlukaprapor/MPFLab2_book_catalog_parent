package com.bookapp.web.controller;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Comment;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;
import com.bookapp.core.exception.BusinessException;
import com.bookapp.core.exception.ValidationException;
import com.bookapp.core.service.CatalogService;
import com.bookapp.core.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring MVC REST контролер для роботи з книгами
 *
 * @RestController - комбінує @Controller і @ResponseBody
 * Всі методи автоматично повертають JSON через HttpMessageConverter
 *
 * @RequestMapping - визначає базовий шлях для всіх ендпоінтів
 */
@RestController
@RequestMapping("/books")
public class BooksController {
    private static final Logger log = LoggerFactory.getLogger(BooksController.class);

    // Ін'єкція залежностей через конструктор (найкращий спосіб)
    private final CatalogService catalogService;
    private final CommentService commentService;

    @Autowired
    public BooksController(CatalogService catalogService, CommentService commentService) {
        this.catalogService = catalogService;
        this.commentService = commentService;
        log.info("BooksController initialized with services");
    }

    /**
     * Пошук книг з пагінацією
     * GET /books?q=java&page=0&size=10&sort=title
     *
     * @RequestParam - прив'язує параметри запиту до аргументів методу
     * required=false - параметр необов'язковий
     * defaultValue - значення за замовчуванням
     */
    @GetMapping
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "title") String sort) {

        log.info("GET /books - query='{}', page={}, size={}, sort={}", query, page, size, sort);

        PageRequest pageRequest = new PageRequest(page, size, sort);
        Page<Book> result = catalogService.searchBooks(query, pageRequest);

        log.info("Found {} books out of {}", result.getContent().size(), result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    /**
     * Отримання деталей книги з коментарями
     * GET /books/{id}
     *
     * @PathVariable - прив'язує змінну з URL до аргументу методу
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookDetails(@PathVariable Long id) {
        log.info("GET /books/{}", id);

        Book book = catalogService.getBookById(id);
        if (book == null) {
            log.warn("Book not found: id={}", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(createError("Book not found"));
        }

        List<Comment> comments = commentService.getCommentsByBookId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("book", book);
        response.put("comments", comments);

        log.info("Returning book {} with {} comments", id, comments.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Додавання коментаря до книги
     * POST /books/{bookId}/comments
     * Content-Type: application/json
     *
     * @RequestBody - десеріалізує JSON з тіла запиту в об'єкт
     */
    @PostMapping("/{bookId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long bookId,
            @RequestBody CommentRequest request) {

        log.info("POST /books/{}/comments - author={}", bookId, request.author);

        try {
            Comment comment = commentService.addComment(
                    bookId,
                    request.author,
                    request.text
            );
            log.info("Comment created: id={}", comment.getId());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(comment);
        } catch (ValidationException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(createError(e.getMessage()));
        }
    }

    /**
     * Видалення коментаря
     * DELETE /books/{bookId}/comments/{commentId}
     */
    @DeleteMapping("/{bookId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long bookId,
            @PathVariable Long commentId) {

        log.info("DELETE /books/{}/comments/{}", bookId, commentId);

        try {
            commentService.deleteComment(commentId);
            log.info("Comment deleted: id={}", commentId);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            log.warn("Business error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(createError(e.getMessage()));
        }
    }

    /**
     * Глобальна обробка ValidationException для цього контролера
     * @ExceptionHandler - перехоплює винятки
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidation(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createError(e.getMessage()));
    }

    /**
     * Глобальна обробка BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusiness(BusinessException e) {
        log.warn("Business error: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(createError(e.getMessage()));
    }

    /**
     * Обробка непередбачуваних помилок
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createError("Internal server error"));
    }

    /**
     * Допоміжний метод для створення помилкових відповідей
     */
    private Map<String, Object> createError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }

    /**
     * DTO для отримання даних коментаря
     */
    static class CommentRequest {
        public String author;
        public String text;
    }
}
