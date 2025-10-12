package com.bookapp.persistence;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;
import com.bookapp.core.port.CatalogRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реалізація репозиторію каталогу із Spring анотацією
 *
 * @Repository - спеціалізована @Component для рівня доступу до даних
 * Spring автоматично обробляє винятки JDBC та перетворює їх у DataAccessException
 */
@Repository
public class CatalogRepository implements CatalogRepositoryPort {
    private static final Logger log = LoggerFactory.getLogger(CatalogRepository.class);

    @Override
    public Page<Book> findBooks(String query, PageRequest pageRequest) {
        log.debug("Finding books: query='{}', page={}, size={}",
                query, pageRequest.getPage(), pageRequest.getSize());

        String sql = buildSearchQuery(query);
        String countSql = buildCountQuery(query);

        try (Connection conn = DatabaseConnection.getConnection()) {
            List<Book> books = executeSearchQuery(conn, sql, query, pageRequest);
            long total = executeCountQuery(conn, countSql, query);

            return new Page<>(books, pageRequest.getPage(), pageRequest.getSize(), total);
        } catch (SQLException e) {
            log.error("Failed to search books", e);
            throw new RuntimeException("Failed to search books", e);
        }
    }

    @Override
    public Optional<Book> findBookById(Long id) {
        log.debug("Finding book by id={}", id);

        String sql = "SELECT id, title, author, isbn, publication_year FROM books WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Book book = mapBook(rs);
                log.debug("Found book: {}", book);
                return Optional.of(book);
            }
            log.debug("Book not found with id={}", id);
            return Optional.empty();
        } catch (SQLException e) {
            log.error("Failed to find book", e);
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

        sql += " ORDER BY " + getSortColumn(pr.getSort()) + " LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int idx = 1;

            if (query != null && !query.trim().isEmpty()) {
                String pattern = "%" + query.toLowerCase() + "%";
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
            }

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