package com.bookapp.core.port;

import com.bookapp.core.domain.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepositoryPort {
    List<Comment> findCommentsByBookId(Long bookId);
    List<Comment> findCommentsByUserId(Long userId);  // Новий метод
    Optional<Comment> findCommentById(Long id);
    Comment addComment(Long bookId, String author, String text);
    boolean deleteComment(Long id);
}