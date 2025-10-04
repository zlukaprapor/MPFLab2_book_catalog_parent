package com.bookapp.persistence;

import com.bookapp.core.domain.Comment;
import com.bookapp.core.port.CommentRepositoryPort;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommentRepository implements CommentRepositoryPort {

    @Override
    public List<Comment> findCommentsByBookId(Long bookId) {
        String sql = "SELECT id, book_id, author, text, created_at " +
                "FROM comments WHERE book_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bookId);
            ResultSet rs = stmt.executeQuery();

            List<Comment> comments = new ArrayList<>();
            while (rs.next()) {
                comments.add(mapComment(rs));
            }
            return comments;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comments", e);
        }
    }

    @Override
    public Optional<Comment> findCommentById(Long id) {
        String sql = "SELECT id, book_id, author, text, created_at FROM comments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapComment(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find comment", e);
        }
    }

    @Override
    public Comment addComment(Long bookId, String author, String text) {
        String sql = "INSERT INTO comments (book_id, author, text, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            stmt.setLong(1, bookId);
            stmt.setString(2, author);
            stmt.setString(3, text);
            stmt.setTimestamp(4, Timestamp.valueOf(now));

            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();

            if (keys.next()) {
                Long id = keys.getLong(1);
                return new Comment(id, bookId, author, text, now);
            }
            throw new RuntimeException("Failed to get generated comment ID");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add comment", e);
        }
    }

    @Override
    public boolean deleteComment(Long id) {
        String sql = "DELETE FROM comments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete comment", e);
        }
    }

    private Comment mapComment(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getLong("id"),
                rs.getLong("book_id"),
                rs.getString("author"),
                rs.getString("text"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}