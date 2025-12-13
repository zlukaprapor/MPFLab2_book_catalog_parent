package com.bookapp.core.service;

import com.bookapp.core.domain.Comment;
import com.bookapp.core.exception.BusinessException;
import com.bookapp.core.exception.CommentTooOldException;
import com.bookapp.core.exception.InvalidCommentDeleteException;
import com.bookapp.core.exception.ValidationException;
import com.bookapp.core.port.CommentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private static final long DELETE_ALLOWED_HOURS = 24;

    private int maxAuthorLength = 100;
    private int maxTextLength = 1000;

    private final CommentRepositoryPort repository;

    public CommentService(CommentRepositoryPort repository) {
        this.repository = repository;
        log.info("CommentService initialized");
    }

    public List<Comment> getCommentsByBookId(Long bookId) {
        log.debug("Getting comments for book_id={}", bookId);
        return repository.findCommentsByBookId(bookId);
    }

    public List<Comment> getCommentsByUserId(Long userId) {
        log.debug("Getting comments for user_id={}", userId);
        return repository.findCommentsByUserId(userId);
    }

    public Comment addComment(Long bookId, String author, String text) {
        validateComment(author, text);

        Comment comment = repository.addComment(bookId, author, text);
        log.info("Comment created: id={}, bookId={}, author={}",
                comment.getId(), bookId, author);
        return comment;
    }

    /**
     * Видалення коментаря з валідацією
     * @param bookId ID книги
     * @param commentId ID коментаря
     * @param createdAt Час створення коментаря
     */
    public void delete(long bookId, long commentId, Instant createdAt) {
        log.debug("Attempting to delete comment: bookId={}, commentId={}, createdAt={}",
                bookId, commentId, createdAt);

        // Валідація ідентифікаторів
        if (bookId <= 0) {
            throw new InvalidCommentDeleteException(
                    "Invalid bookId: " + bookId + ". BookId must be positive");
        }

        if (commentId <= 0) {
            throw new InvalidCommentDeleteException(
                    "Invalid commentId: " + commentId + ". CommentId must be positive");
        }

        // Валідація наявності createdAt
        if (createdAt == null) {
            throw new InvalidCommentDeleteException(
                    "CreatedAt timestamp is required for comment deletion");
        }

        // Перевірка віку коментаря
        Instant now = Instant.now();
        Duration age = Duration.between(createdAt, now);

        if (age.toHours() > DELETE_ALLOWED_HOURS) {
            throw new CommentTooOldException(
                    String.format("Коментар створено більше ніж %d години тому і не може бути видалений. " +
                                    "Вік коментаря: %d годин",
                            DELETE_ALLOWED_HOURS, age.toHours()));
        }

        // Якщо всі перевірки пройдені - видаляємо
        boolean deleted = repository.deleteComment(commentId);

        if (deleted) {
            log.info("Comment deleted successfully: bookId={}, commentId={}", bookId, commentId);
        } else {
            log.warn("Comment not found for deletion: bookId={}, commentId={}", bookId, commentId);
        }
    }

    /**
     * Старий метод видалення (для зворотної сумісності)
     */
    public void deleteComment(Long commentId) {
        Comment comment = repository.findCommentById(commentId)
                .orElseThrow(() -> new BusinessException("Comment not found"));

        Duration age = Duration.between(comment.getCreatedAt(), LocalDateTime.now());
        if (age.toHours() > DELETE_ALLOWED_HOURS) {
            throw new BusinessException(
                    "Cannot delete comment older than " + DELETE_ALLOWED_HOURS + " hours");
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