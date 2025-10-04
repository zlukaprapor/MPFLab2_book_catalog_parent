package com.bookapp.core.service;

import com.bookapp.core.domain.Comment;
import com.bookapp.core.exception.BusinessException;
import com.bookapp.core.exception.ValidationException;
import com.bookapp.core.port.CommentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private static final int MAX_AUTHOR_LENGTH = 100;
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final long DELETE_ALLOWED_HOURS = 24;

    private final CommentRepositoryPort repository;

    public CommentService(CommentRepositoryPort repository) {
        this.repository = repository;
    }

    public List<Comment> getCommentsByBookId(Long bookId) {
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
        if (age.toHours() > DELETE_ALLOWED_HOURS) {
            throw new BusinessException("Cannot delete comment older than 24 hours");
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
        if (author.length() > MAX_AUTHOR_LENGTH) {
            throw new ValidationException("Author too long (max " + MAX_AUTHOR_LENGTH + " chars)");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Text is required");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new ValidationException("Text too long (max " + MAX_TEXT_LENGTH + " chars)");
        }
    }
}