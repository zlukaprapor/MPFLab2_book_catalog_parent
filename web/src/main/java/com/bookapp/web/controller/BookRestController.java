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
 * REST контролер для роботи з книгами та коментарями
 * ВИПРАВЛЕНО: додано явні назви параметрів у @RequestParam
 */
@RestController
@RequestMapping("/api")
public class BookRestController {
    private static final Logger log = LoggerFactory.getLogger(BookRestController.class);

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private CommentService commentService;

    /**
     * Пошук книг з пагінацією
     * GET /api/books?q=java&page=0&size=10&sort=title
     */
    @GetMapping("/books")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "title") String sort) {

        log.info("GET /api/books - q='{}', page={}, size={}, sort={}", q, page, size, sort);

        PageRequest pageRequest = new PageRequest(page, size, sort);
        Page<Book> result = catalogService.searchBooks(q, pageRequest);

        log.info("Returning {} books (total: {})", result.getContent().size(), result.getTotalElements());

        return ResponseEntity.ok(result);
    }

    /**
     * Отримання деталей книги з коментарями
     * GET /api/books/{id}
     */
    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookDetails(@PathVariable Long id) {
        log.info("GET /api/books/{}", id);

        Book book = catalogService.getBookById(id);
        if (book == null) {
            log.warn("Book not found: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Book not found"));
        }

        List<Comment> comments = commentService.getCommentsByBookId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("book", book);
        response.put("comments", comments);

        log.info("Returning book {} with {} comments", id, comments.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Додавання коментаря
     * POST /api/books/{bookId}/comments
     */
    @PostMapping("/books/{bookId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long bookId,
            @RequestBody CommentRequest request) {

        log.info("POST /api/books/{}/comments - author={}", bookId, request.author);

        try {
            Comment comment = commentService.addComment(
                    bookId,
                    request.author,
                    request.text
            );
            log.info("Comment created: id={}", comment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (ValidationException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Видалення коментаря
     * DELETE /api/books/{bookId}/comments/{commentId}
     */
    @DeleteMapping("/books/{bookId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long bookId,
            @PathVariable Long commentId) {

        log.info("DELETE /api/books/{}/comments/{}", bookId, commentId);

        try {
            commentService.deleteComment(commentId);
            log.info("Comment deleted: id={}", commentId);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            log.warn("Business error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Глобальна обробка помилок ValidationException
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException e) {
        log.warn("Validation error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
    }

    /**
     * Глобальна обробка помилок BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e) {
        log.warn("Business error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(e.getMessage()));
    }

    /**
     * Обробка непередбачуваних помилок
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("An unexpected error occurred"));
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }

    // DTO для запиту коментаря
    static class CommentRequest {
        public String author;
        public String text;
    }
}