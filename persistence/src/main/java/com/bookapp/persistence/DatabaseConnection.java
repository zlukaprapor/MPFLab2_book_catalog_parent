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

            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "author VARCHAR(255) NOT NULL, " +
                    "isbn VARCHAR(20), " +
                    "year INT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS comments (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "book_id BIGINT NOT NULL, " +
                    "author VARCHAR(100) NOT NULL, " +
                    "text VARCHAR(1000) NOT NULL, " +
                    "created_at TIMESTAMP NOT NULL, " +
                    "FOREIGN KEY (book_id) REFERENCES books(id))");

            // Sample books
            stmt.execute("INSERT INTO books (title, author, isbn, year) VALUES " +
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

            // Sample comments (varied ages for testing 24h deletion rule)
            stmt.execute("INSERT INTO comments (book_id, author, text, created_at) VALUES " +
                    // Recent comments (can be deleted)
                    "(1, 'Олена', 'Обов''язкова книга для кожного програміста! Дуже детально пояснює принципи чистого коду.', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)), " +
                    "(1, 'Дмитро', 'Читаю вже втретє. Щоразу знаходжу щось нове. Рекомендую!', DATEADD('HOUR', -5, CURRENT_TIMESTAMP)), " +
                    "(2, 'Ірина', 'Класика! Всі патерни добре описані з прикладами. Трохи застарілі приклади, але принципи актуальні.', DATEADD('HOUR', -1, CURRENT_TIMESTAMP)), " +
                    "(3, 'Максим', 'Найкраща книга по Java! Joshua Bloch - справжній майстер.', DATEADD('HOUR', -8, CURRENT_TIMESTAMP)), " +
                    "(3, 'Світлана', 'Дуже практичні поради. Після прочитання код став набагато кращим.', DATEADD('HOUR', -12, CURRENT_TIMESTAMP)), " +
                    // Older comments (cannot be deleted - older than 24h)
                    "(4, 'Андрій', 'Ця книга змінила моє бачення програмування. Must read!', DATEADD('DAY', -2, CURRENT_TIMESTAMP)), " +
                    "(5, 'Наталія', 'Рефакторинг тепер не здається таким страшним. Дякую автору за чудову книгу!', DATEADD('DAY', -3, CURRENT_TIMESTAMP)), " +
                    "(6, 'Петро', 'Найкраща книга для початківців! Все дуже зрозуміло пояснюється.', DATEADD('DAY', -5, CURRENT_TIMESTAMP)), " +
                    "(7, 'Юлія', 'Об''ємна, але дуже корисна книга. Брюс Еккель - чудовий викладач.', DATEADD('DAY', -7, CURRENT_TIMESTAMP)), " +
                    "(8, 'Віктор', 'Багатопоточність стала зрозумілою після цієї книги. Highly recommended!', DATEADD('DAY', -1, CURRENT_TIMESTAMP)), " +
                    "(9, 'Марина', 'Spring Framework розкритий дуже детально. Гарна книга для вивчення.', DATEADD('HOUR', -20, CURRENT_TIMESTAMP)), " +
                    "(10, 'Сергій', 'Hibernate - складна тема, але книга допомогла розібратися.', DATEADD('DAY', -10, CURRENT_TIMESTAMP)), " +
                    // More comments for popular books
                    "(1, 'Оксана', 'Перечитую кожного року. Завжди актуально!', DATEADD('HOUR', -15, CURRENT_TIMESTAMP)), " +
                    "(2, 'Ігор', 'Патерни проектування - основа основ. Дякую за класику!', DATEADD('DAY', -4, CURRENT_TIMESTAMP)), " +
                    "(3, 'Тетяна', 'Item 17 про іммутабельність - просто геніально описано!', DATEADD('HOUR', -6, CURRENT_TIMESTAMP))");

            log.info("Database schema initialized");
        } catch (SQLException e) {
            log.error("Failed to initialize schema", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}