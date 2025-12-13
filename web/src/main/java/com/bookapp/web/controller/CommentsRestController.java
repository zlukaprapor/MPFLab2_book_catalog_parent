package com.bookapp.web.controller;

import com.bookapp.core.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * REST API контролер для роботи з коментарями
 */
@RestController
@RequestMapping("/api/comments")
public class CommentsRestController {

    @Autowired
    private CommentService commentService;

    /**
     * Видалення коментаря через REST API
     * POST /api/comments/delete
     *
     * @param bookId ID книги
     * @param commentId ID коментаря
     * @param createdAt Час створення коментаря (ISO-8601 format)
     * @return Статус операції
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteComment(
            @RequestParam("bookId") long bookId,
            @RequestParam("commentId") long commentId,
            @RequestParam("createdAt") String createdAt) {

        // Парсинг Instant з ISO-8601 формату
        Instant timestamp = Instant.parse(createdAt);

        // Виклик сервісу (винятки оброблятимуться GlobalApiExceptionHandler)
        commentService.delete(bookId, commentId, timestamp);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Comment deleted successfully");

        return ResponseEntity.ok(response);
    }
}