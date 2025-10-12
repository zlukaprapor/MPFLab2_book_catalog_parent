package com.bookapp.core.service;

import com.bookapp.core.domain.Comment;
import com.bookapp.core.exception.BusinessException;
import com.bookapp.core.exception.ValidationException;
import com.bookapp.core.port.CommentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервіс коментарів із використанням Spring DI та конфігурації
 */
@Service
public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    // Ін'єкція значень із application.properties
    @Value("${comments.max-author-length:100}")
    private int maxAuthorLength;

    @Value("${comments.max-text-length:1000}")
    private int maxTextLength;

    @Value("${comments.delete-allowed-hours:24}")
    private long deleteAllowedHours;

    private final CommentRepositoryPort repository;

    /**
     * Ін'єкція через конструктор (найкращий спосіб)
     */
    @Autowired
    public CommentService(CommentRepositoryPort repository) {
        this.repository = repository;
        log.info("CommentService initialized");
    }

    public List<Comment> getCommentsByBookId(Long bookId) {
        log.debug("Getting comments for book_id={}", bookId);
        return repository.findCommentsByBookId(bookId);
    }

    public Comment addComment(Long bookId, String author, String text) {
        validateComment(author, text);

        Comment comment = repository.addComment(bookId, author, text);
        log.info("Comment created: id={}, bookId={}, author={}",
                comment.getId(), bookId, author);
        return comment;
    }

    public void deleteComment(Long commentId) {
        Comment comment = repository.findCommentById(commentId)
                .orElseThrow(() -> new BusinessException("Comment not found"));

        Duration age = Duration.between(comment.getCreatedAt(), LocalDateTime.now());
        if (age.toHours() > deleteAllowedHours) {
            throw new BusinessException(
                    "Cannot delete comment older than " + deleteAllowedHours + " hours");
        }

        boolean deleted = repository.deleteComment(commentId);
        if (deleted) {
            log.info("Comment deleted: id={}, bookId={}", commentId, comment.getBookId());
        }
    }

    private void validateComment(String author, String text) {
        if (author == null || author.trim().isEmpty()) {
            throw new ValidationException("Author is required");
        }
        if (author.length() > maxAuthorLength) {
            throw new ValidationException(
                    "Author too long (max " + maxAuthorLength + " chars)");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Text is required");
        }
        if (text.length() > maxTextLength) {
            throw new ValidationException(
                    "Text too long (max " + maxTextLength + " chars)");
        }
    }
}