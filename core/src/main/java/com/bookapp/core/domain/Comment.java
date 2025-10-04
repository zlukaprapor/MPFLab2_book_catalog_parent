package com.bookapp.core.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Comment {
    private final Long id;
    private final Long bookId;
    private final String author;
    private final String text;
    private final LocalDateTime createdAt;

    public Comment(Long id, Long bookId, String author, String text, LocalDateTime createdAt) {
        this.id = id;
        this.bookId = bookId;
        this.author = author;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Comment{id=" + id + ", bookId=" + bookId + ", author='" + author + "'}";
    }
}