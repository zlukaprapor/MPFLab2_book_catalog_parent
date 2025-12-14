-- ===============================
-- Таблиця користувачів
-- ===============================
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',
    enabled BOOLEAN DEFAULT TRUE,
    email VARCHAR(255) UNIQUE
);

-- ===============================
-- Таблиця книг
-- ===============================
CREATE TABLE books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20),
    publish_year INT
);

-- ===============================
-- Таблиця коментарів
-- ===============================
CREATE TABLE comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_book
        FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ===============================
-- Таблиця токенів підтвердження email
-- ===============================
CREATE TABLE confirmation_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    CONSTRAINT fk_confirmation_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ===============================
-- Індекси
-- ===============================
CREATE INDEX idx_comments_book_id ON comments(book_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_confirmation_token ON confirmation_tokens(token);

-- ===============================
-- Тестові користувачі
-- ===============================
INSERT INTO users (username, password, role, enabled, email) VALUES
    ('ivan', 'password123', 'USER', TRUE, 'ivan@example.com'),
    ('maria', 'password123', 'USER', TRUE, 'maria@example.com'),
    ('olena', 'password123', 'USER', TRUE, 'olena@example.com'),
    ('dmytro', 'password123', 'USER', TRUE, 'dmytro@example.com'),
    ('admin', 'admin123', 'ADMIN', TRUE, 'admin@example.com');

-- ===============================
-- Тестові книги
-- ===============================
INSERT INTO books (title, author, isbn, publish_year) VALUES
   ('Clean Code', 'Robert C. Martin', '978-0132350884', 2008),
   ('Effective Java', 'Joshua Bloch', '978-0134685991', 2018),
   ('The Pragmatic Programmer', 'Andrew Hunt, David Thomas', '978-0135957059', 2019),
   ('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 2009),
   ('Design Patterns', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', '978-0201633610', 1994),
   ('Java Concurrency in Practice', 'Brian Goetz', '978-0321349606', 2006),
   ('Python Crash Course', 'Eric Matthes', '978-1593279288', 2019),
   ('JavaScript: The Good Parts', 'Douglas Crockford', '978-0596517748', 2008),
   ('You Dont Know JS Yet', 'Kyle Simpson', '978-1091210092', 2020),
   ('Head First Design Patterns', 'Eric Freeman, Elisabeth Robson', '978-0596007126', 2004),
   ('Refactoring', 'Martin Fowler', '978-0201485677', 1999),
   ('Code Complete', 'Steve McConnell', '978-0735619678', 2004),
   ('Cracking the Coding Interview', 'Gayle Laakmann McDowell', '978-0984782857', 2015),
   ('Algorithms', 'Robert Sedgewick, Kevin Wayne', '978-0321573513', 2011),
   ('Grokking Algorithms', 'Aditya Bhargava', '978-1617292231', 2016),
   ('Introduction to Machine Learning with Python', 'Andreas C. Müller, Sarah Guido', '978-1449369415', 2016),
   ('Artificial Intelligence: A Modern Approach', 'Stuart Russell, Peter Norvig', '978-0136042594', 2010),
   ('Deep Learning', 'Ian Goodfellow, Yoshua Bengio, Aaron Courville', '978-0262035613', 2016),
   ('Effective C++', 'Scott Meyers', '978-0321334879', 2005),
   ('Programming Pearls', 'Jon Bentley', '978-0201657883', 1999);

-- ===============================
-- Тестові коментарі
-- ===============================
INSERT INTO comments (book_id, user_id, text) VALUES
    (1, 1, 'Чудова книга! Навчає писати чистий код без зайвих повторів.'),
    (1, 2, 'Роберт Мартін дійсно знає свою справу, рекомендую!'),

    (2, 2, 'Effective Java – обовязкова для всіх Java-розробників.'),
    (2, 3, 'Пройшов через багато практик із цієї книги, дуже корисно.'),

    (3, 3, 'Pragmatic Programmer надихає підходом до кодування та карєри.'),
    (3, 4, 'Чудова книга для розвитку мислення програміста.'),

    (4, 1, 'Алгоритми тут пояснені дуже детально, дуже корисно для студентів.'),
    (4, 5, 'Добре підібрані приклади, легко зрозуміти навіть складні алгоритми.'),

    (5, 2, 'Design Patterns допомагає писати більш гнучкий та підтримуваний код.'),
    (5, 4, 'Обовязково до прочитання для всіх розробників.'),

    (6, 1, 'Concurrency в Java – складна тема, але ця книга робить її зрозумілою.'),
    (6, 3, 'Завдяки цій книзі навчився уникати багатьох пасток багатопоточності.'),

    (7, 2, 'Python Crash Course – відмінна книга для початківців у Python.'),
    (7, 5, 'Чудові вправи та приклади, легко почати програмувати.'),

    (8, 1, 'JS: The Good Parts дуже корисна для вивчення чистого JavaScript.'),
    (8, 4, 'Рекомендую всім, хто хоче писати якісний JS код.'),

    (9, 3, 'You Dont Know JS Yet відкриває справжню глибину JS.'),
    (9, 5, 'Дуже детально про замикання та асинхронність, просто must-read.'),

    (10, 1, 'Head First Design Patterns подано наочно та весело, дуже допомагає.'),
    (10, 2, 'Книга реально запамятовується завдяки прикладам і картинкам.'),

    (11, 3, 'Refactoring навчив мене покращувати код без страху зламати все.'),
    (11, 4, 'Класна книга для поліпшення існуючих проектів.'),

    (12, 1, 'Code Complete – справжній майстер-клас по розробці ПЗ.'),
    (12, 5, 'Дуже детальні поради по структурі коду і дизайну.'),

    (13, 2, 'Cracking the Coding Interview допомогла пройти співбесіду у великій компанії.'),
    (13, 4, 'Чудові питання та пояснення, дуже корисно для підготовки.'),

    (14, 3, 'Algorithms від Sedgewick – чудове поєднання теорії та практики.'),
    (14, 5, 'Приклади реально допомагають закріпити матеріал.'),

    (15, 1, 'Grokking Algorithms просто і наочно пояснює складні алгоритми.'),
    (15, 4, 'Легко читати, гарно структурована, особливо для новачків.'),

    (16, 2, 'ML з Python зрозумілий навіть для початківців.'),
    (16, 5, 'Класна подача, багато прикладів з реальних задач.'),

    (17, 1, 'AI: A Modern Approach – біблія для всіх, хто цікавиться ШІ.'),
    (17, 3, 'Великий обсяг інформації, але дуже корисно.'),

    (18, 2, 'Deep Learning – дуже детальна книга, багато прикладів та концепцій.'),
    (18, 4, 'Підійде як для студентів, так і для досвідчених практиків.'),

    (19, 1, 'Effective C++ містить безліч корисних порад.'),
    (19, 5, 'Реальні приклади, які допомагають уникати помилок.'),

    (20, 2, 'Programming Pearls – класика, яка досі актуальна.'),
    (20, 3, 'Коротко, але дуже змістовно.');
