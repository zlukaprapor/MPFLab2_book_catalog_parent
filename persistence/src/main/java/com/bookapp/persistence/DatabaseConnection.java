package com.bookapp.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final String URL = "jdbc:h2:mem:bookdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // ✅ ВИДАЛІТЬ СТАРІ ТАБЛИЦІ (DROP IF EXISTS)
            stmt.execute("DROP TABLE IF EXISTS comments");
            stmt.execute("DROP TABLE IF EXISTS books");

            // ✅ СТВОРІТЬ НОВІ ТАБЛИЦІ
            stmt.execute("CREATE TABLE books (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "author VARCHAR(255) NOT NULL, " +
                    "isbn VARCHAR(20), " +
                    "publication_year INT)");

            stmt.execute("CREATE TABLE comments (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "book_id BIGINT NOT NULL, " +
                    "author VARCHAR(100) NOT NULL, " +
                    "text VARCHAR(1000) NOT NULL, " +
                    "created_at TIMESTAMP NOT NULL, " +
                    "FOREIGN KEY (book_id) REFERENCES books(id))");

            // Sample books
            stmt.execute("INSERT INTO books (title, author, isbn, publication_year) VALUES " +
                    "('Clean Code', 'Robert Martin', '978-0132350884', 2008), " +
                    "('Design Patterns', 'Gang of Four', '978-0201633610', 1994), " +
                    "('Effective Java', 'Joshua Bloch', '978-0134685991', 2017), " +
                    "('The Pragmatic Programmer', 'Hunt & Thomas', '978-0135957059', 2019), " +
                    "('Refactoring', 'Martin Fowler', '978-0134757599', 2018), " +
                    "('Head First Java', 'Kathy Sierra', '978-0596009205', 2005), " +
                    "('Thinking in Java', 'Bruce Eckel', '978-0131872486', 2006), " +
                    "('Java Concurrency in Practice', 'Brian Goetz', '978-0321349606', 2006), " +
                    "('Spring in Action', 'Craig Walls', '978-1617294945', 2018), " +
                    "('Hibernate in Action', 'Christian Bauer', '978-1932394153', 2004)");

            // Sample comments
            stmt.execute("INSERT INTO comments (book_id, author, text, created_at) VALUES " +
                    "(1, 'Олена', 'Обов''язкова книга для кожного програміста!', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)), " +
                    "(1, 'Дмитро', 'Читаю вже втретє. Щоразу знаходжу щось нове.', DATEADD('HOUR', -5, CURRENT_TIMESTAMP)), " +
                    "(2, 'Ірина', 'Класика! Всі патерни добре описані з прикладами.', DATEADD('HOUR', -1, CURRENT_TIMESTAMP)), " +
                    "(3, 'Максим', 'Найкраща книга по Java!', DATEADD('HOUR', -8, CURRENT_TIMESTAMP)), " +
                    "(3, 'Світлана', 'Дуже практичні поради.', DATEADD('HOUR', -12, CURRENT_TIMESTAMP)), " +
                    "(4, 'Андрій', 'Ця книга змінила моє бачення програмування.', DATEADD('DAY', -2, CURRENT_TIMESTAMP)), " +
                    "(5, 'Наталія', 'Рефакторинг тепер не здається таким страшним.', DATEADD('DAY', -3, CURRENT_TIMESTAMP)), " +
                    "(6, 'Петро', 'Найкраща книга для початківців!', DATEADD('DAY', -5, CURRENT_TIMESTAMP)), " +
                    "(7, 'Юлія', 'Об''ємна, але дуже корисна книга.', DATEADD('DAY', -7, CURRENT_TIMESTAMP)), " +
                    "(8, 'Віктор', 'Багатопоточність стала зрозумілою після цієї книги.', DATEADD('DAY', -1, CURRENT_TIMESTAMP)), " +
                    "(9, 'Марина', 'Spring Framework розкритий дуже детально.', DATEADD('HOUR', -20, CURRENT_TIMESTAMP)), " +
                    "(10, 'Сергій', 'Hibernate - складна тема, але книга допомогла розібратися.', DATEADD('DAY', -10, CURRENT_TIMESTAMP))");

            log.info("Database schema initialized");
        } catch (SQLException e) {
            log.error("Failed to initialize schema", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
