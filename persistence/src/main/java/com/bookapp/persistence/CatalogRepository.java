package com.bookapp.persistence;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;
import com.bookapp.core.port.CatalogRepositoryPort;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CatalogRepository implements CatalogRepositoryPort {

    @Override
    public Page<Book> findBooks(String query, PageRequest pageRequest) {
        String sql = buildSearchQuery(query);
        String countSql = buildCountQuery(query);

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Book> books = executeSearchQuery(conn, sql, query, pageRequest);
            long total = executeCountQuery(conn, countSql, query);

            return new Page<>(books, pageRequest.getPage(), pageRequest.getSize(), total);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search books", e);
        }
    }

    @Override
    public Optional<Book> findBookById(Long id) {
        String sql = "SELECT id, title, author, isbn, publication_year FROM books WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapBook(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find book", e);
        }
    }

    private String buildSearchQuery(String query) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, title, author, isbn, publication_year FROM books"
        );

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ?");
        }

        return sql.toString();
    }

    private String buildCountQuery(String query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM books");
        if (query != null && !query.trim().isEmpty()) {
            sql.append(" WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ?");
        }
        return sql.toString();
    }

    private List<Book> executeSearchQuery(Connection conn, String sql, String query, PageRequest pr)
            throws SQLException {

        // Додаємо ORDER BY, LIMIT, OFFSET
        sql += " ORDER BY " + getSortColumn(pr.getSort()) + " LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int idx = 1;

            // Параметри для WHERE
            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.toLowerCase() + "%";
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
            }

            // Параметри для LIMIT і OFFSET
            stmt.setInt(idx++, pr.getSize());
            stmt.setInt(idx, pr.getPage() * pr.getSize());

            List<Book> books = new ArrayList<>();
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                books.add(mapBook(rs));
            }
            return books;
        }
    }

    private long executeCountQuery(Connection conn, String sql, String query) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.toLowerCase() + "%";
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);
            }

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private String getSortColumn(String sort) {
        if ("author".equals(sort)) return "author";
        if ("year".equals(sort)) return "publication_year";
        return "title";
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getInt("publication_year")
        );
    }
}