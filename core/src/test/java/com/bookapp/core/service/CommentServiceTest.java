package com.bookapp.core.service;

import com.bookapp.core.exception.CommentTooOldException;
import com.bookapp.core.exception.InvalidCommentDeleteException;
import com.bookapp.core.port.CommentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Юніт-тести для CommentService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Tests")
class CommentServiceTest {

    @Mock
    private CommentRepositoryPort commentRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentRepository);
    }

    // ========== Тести для валідації параметрів ==========

    @Test
    @DisplayName("Should throw InvalidCommentDeleteException when bookId is zero")
    void shouldThrowExceptionWhenBookIdIsZero() {
        // Given
        long bookId = 0;
        long commentId = 1;
        Instant createdAt = Instant.now();

        // When & Then
        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        assertTrue(exception.getMessage().contains("Invalid bookId"));
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw InvalidCommentDeleteException when bookId is negative")
    void shouldThrowExceptionWhenBookIdIsNegative() {
        // Given
        long bookId = -5;
        long commentId = 1;
        Instant createdAt = Instant.now();

        // When & Then
        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        assertTrue(exception.getMessage().contains("Invalid bookId"));
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw InvalidCommentDeleteException when commentId is zero")
    void shouldThrowExceptionWhenCommentIdIsZero() {
        // Given
        long bookId = 1;
        long commentId = 0;
        Instant createdAt = Instant.now();

        // When & Then
        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        assertTrue(exception.getMessage().contains("Invalid commentId"));
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw InvalidCommentDeleteException when commentId is negative")
    void shouldThrowExceptionWhenCommentIdIsNegative() {
        // Given
        long bookId = 1;
        long commentId = -10;
        Instant createdAt = Instant.now();

        // When & Then
        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        assertTrue(exception.getMessage().contains("Invalid commentId"));
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw InvalidCommentDeleteException when createdAt is null")
    void shouldThrowExceptionWhenCreatedAtIsNull() {
        // Given
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = null;

        // When & Then
        InvalidCommentDeleteException exception = assertThrows(
                InvalidCommentDeleteException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        assertTrue(exception.getMessage().contains("CreatedAt timestamp is required"));
        verifyNoInteractions(commentRepository);
    }

    // ========== Тести для перевірки віку коментаря ==========

    @Test
    @DisplayName("Should throw CommentTooOldException when comment is older than 24 hours")
    void shouldThrowExceptionWhenCommentIsTooOld() {
        // Given
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(25, ChronoUnit.HOURS);

        // When & Then
        CommentTooOldException exception = assertThrows(
                CommentTooOldException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        assertTrue(exception.getMessage().contains("більше ніж 24 години тому"));
        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw CommentTooOldException when comment is exactly 25 hours old")
    void shouldThrowExceptionWhenCommentIs25HoursOld() {
        // Given
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(25, ChronoUnit.HOURS);

        // When & Then
        assertThrows(
                CommentTooOldException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        verifyNoInteractions(commentRepository);
    }

    @Test
    @DisplayName("Should throw CommentTooOldException when comment is 48 hours old")
    void shouldThrowExceptionWhenCommentIs48HoursOld() {
        // Given
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(48, ChronoUnit.HOURS);

        // When & Then
        assertThrows(
                CommentTooOldException.class,
                () -> commentService.delete(bookId, commentId, createdAt)
        );

        verifyNoInteractions(commentRepository);
    }

    // ========== Тести для успішного видалення ==========

    @Test
    @DisplayName("Should successfully delete comment when all parameters are valid")
    void shouldDeleteCommentWhenParametersAreValid() {
        // Given
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(1, ChronoUnit.HOURS);

        when(commentRepository.deleteComment(commentId)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> commentService.delete(bookId, commentId, createdAt));

        // Then
        verify(commentRepository, times(1)).deleteComment(commentId);
    }

    @Test
    @DisplayName("Should successfully delete comment created 23 hours ago")
    void shouldDeleteCommentCreated23HoursAgo() {
        // Given
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(23, ChronoUnit.HOURS);

        when(commentRepository.deleteComment(commentId)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> commentService.delete(bookId, commentId, createdAt));

        // Then
        verify(commentRepository, times(1)).deleteComment(commentId);
    }

    @Test
    @DisplayName("Should successfully delete comment created 1 minute ago")
    void shouldDeleteCommentCreated1MinuteAgo() {
        // Given
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now().minus(1, ChronoUnit.MINUTES);

        when(commentRepository.deleteComment(commentId)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> commentService.delete(bookId, commentId, createdAt));

        // Then
        verify(commentRepository, times(1)).deleteComment(commentId);
    }

    @Test
    @DisplayName("Should successfully delete comment created just now")
    void shouldDeleteCommentCreatedJustNow() {
        // Given
        long bookId = 1;
        long commentId = 2;
        Instant createdAt = Instant.now();

        when(commentRepository.deleteComment(commentId)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> commentService.delete(bookId, commentId, createdAt));

        // Then
        verify(commentRepository, times(1)).deleteComment(commentId);
    }
}