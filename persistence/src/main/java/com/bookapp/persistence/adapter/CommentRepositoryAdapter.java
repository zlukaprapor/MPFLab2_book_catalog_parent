package com.bookapp.persistence.adapter;

import com.bookapp.core.domain.Comment;
import com.bookapp.core.port.CommentRepositoryPort;
import com.bookapp.persistence.entity.BookEntity;
import com.bookapp.persistence.entity.CommentEntity;
import com.bookapp.persistence.entity.UserEntity;
import com.bookapp.persistence.repository.BookRepository;
import com.bookapp.persistence.repository.CommentRepository;
import com.bookapp.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommentRepositoryAdapter implements CommentRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(CommentRepositoryAdapter.class);

    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public CommentRepositoryAdapter(
            CommentRepository commentRepository,
            BookRepository bookRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        log.info("CommentRepositoryAdapter initialized");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findCommentsByBookId(Long bookId) {
        return commentRepository.findByBookId(bookId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findCommentsByUserId(Long userId) {
        return commentRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comment> findCommentById(Long id) {
        return commentRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional
    public Comment addComment(Long bookId, String author, String text) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        // Знаходимо або створюємо користувача за іменем
        UserEntity user = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(author))
                .findFirst()
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setUsername(author);
                    newUser.setPassword("default"); // В реальному додатку - хешування
                    newUser.setRole("USER");
                    return userRepository.save(newUser);
                });

        CommentEntity comment = new CommentEntity();
        comment.setBook(book);
        comment.setUser(user);
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());

        CommentEntity saved = commentRepository.save(comment);
        log.info("Comment added: id={}, bookId={}, userId={}", saved.getId(), bookId, user.getId());

        return toDomain(saved);
    }

    @Override
    @Transactional
    public boolean deleteComment(Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            log.info("Comment deleted: id={}", id);
            return true;
        }
        return false;
    }

    private Comment toDomain(CommentEntity entity) {
        return new Comment(
                entity.getId(),
                entity.getBook().getId(),
                entity.getUser().getUsername(),
                entity.getText(),
                entity.getCreatedAt()
        );
    }
}