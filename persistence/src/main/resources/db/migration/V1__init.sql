-- Створення таблиці користувачів
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER'
);

-- Створення таблиці книг
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20),
    publish_year INT
);

-- Створення таблиці коментарів
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Індекси для оптимізації запитів
CREATE INDEX idx_comments_book_id ON comments(book_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);

-- Тестові дані: користувачі
INSERT INTO users (username, password, role) VALUES
    ('ivan', 'password123', 'USER'),
    ('maria', 'password123', 'USER'),
    ('olena', 'olena123', 'USER'),
    ('dmytro', 'dmytro123', 'USER'),
    ('admin', 'admin123', 'ADMIN');

-- Тестові дані: книги
INSERT INTO books (title, author, isbn, publish_year) VALUES
   ('Clean Code', 'Robert C. Martin', '978-0132350884', 2008),
   ('Effective Java', 'Joshua Bloch', '978-0134685991', 2018),
   ('The Pragmatic Programmer', 'Andrew Hunt, David Thomas', '978-0135957059', 2019),
   ('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 2009),
   ('Design Patterns', 'Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides', '978-0201633610', 1994),
   ('Java Concurrency in Practice', 'Brian Goetz', '978-0321349606', 2006),
   ('Python Crash Course', 'Eric Matthes', '978-1593279288', 2019),
   ('JavaScript: The Good Parts', 'Douglas Crockford', '978-0596517748', 2008),
   ('You Don’t Know JS Yet', 'Kyle Simpson', '978-1091210092', 2020),
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
-- Тестові дані: коментарі
INSERT INTO comments (book_id, user_id, text, created_at) VALUES
    (1, 1, 'Чудова книга! Навчає писати чистий код без зайвих повторів.', CURRENT_TIMESTAMP),
    (1, 2, 'Роберт Мартін дійсно знає свою справу, рекомендую!', CURRENT_TIMESTAMP),

    (2, 2, 'Effective Java – обов’язкова для всіх Java-розробників.', CURRENT_TIMESTAMP),
    (2, 3, 'Пройшов через багато практик із цієї книги, дуже корисно.', CURRENT_TIMESTAMP),

    (3, 3, 'Pragmatic Programmer надихає підходом до кодування та кар’єри.', CURRENT_TIMESTAMP),
    (3, 4, 'Чудова книга для розвитку мислення програміста.', CURRENT_TIMESTAMP),

    (4, 1, 'Алгоритми тут пояснені дуже детально, дуже корисно для студентів.', CURRENT_TIMESTAMP),
    (4, 5, 'Добре підібрані приклади, легко зрозуміти навіть складні алгоритми.', CURRENT_TIMESTAMP),

    (5, 2, 'Design Patterns допомагає писати більш гнучкий та підтримуваний код.', CURRENT_TIMESTAMP),
    (5, 4, 'Обов’язково до прочитання для всіх розробників.', CURRENT_TIMESTAMP),

    (6, 1, 'Concurrency в Java – складна тема, але ця книга робить її зрозумілою.', CURRENT_TIMESTAMP),
    (6, 3, 'Завдяки цій книзі навчився уникати багатьох пасток багатопоточності.', CURRENT_TIMESTAMP),

    (7, 2, 'Python Crash Course – відмінна книга для початківців у Python.', CURRENT_TIMESTAMP),
    (7, 5, 'Чудові вправи та приклади, легко почати програмувати.', CURRENT_TIMESTAMP),

    (8, 1, 'JS: The Good Parts дуже корисна для вивчення чистого JavaScript.', CURRENT_TIMESTAMP),
    (8, 4, 'Рекомендую всім, хто хоче писати якісний JS код.', CURRENT_TIMESTAMP),

    (9, 3, 'You Don’t Know JS Yet відкриває справжню глибину JS.', CURRENT_TIMESTAMP),
    (9, 5, 'Дуже детально про замикання та асинхронність, просто must-read.', CURRENT_TIMESTAMP),

    (10, 1, 'Head First Design Patterns подано наочно та весело, дуже допомагає.', CURRENT_TIMESTAMP),
    (10, 2, 'Книга реально запам’ятовується завдяки прикладам і картинкам.', CURRENT_TIMESTAMP),

    (11, 3, 'Refactoring навчив мене покращувати код без страху зламати все.', CURRENT_TIMESTAMP),
    (11, 4, 'Класна книга для поліпшення існуючих проектів.', CURRENT_TIMESTAMP),

    (12, 1, 'Code Complete – справжній майстер-клас по розробці програмного забезпечення.', CURRENT_TIMESTAMP),
    (12, 5, 'Дуже детальні поради по структурі коду і дизайну.', CURRENT_TIMESTAMP),

    (13, 2, 'Cracking the Coding Interview допомогла пройти співбесіду у великій компанії.', CURRENT_TIMESTAMP),
    (13, 4, 'Чудові питання та пояснення, дуже корисно для підготовки.', CURRENT_TIMESTAMP),

    (14, 3, 'Algorithms від Sedgewick – чудове поєднання теорії та практики.', CURRENT_TIMESTAMP),
    (14, 5, 'Приклади реально допомагають закріпити матеріал.', CURRENT_TIMESTAMP),

    (15, 1, 'Grokking Algorithms просто і наочно пояснює складні алгоритми.', CURRENT_TIMESTAMP),
    (15, 4, 'Легко читати, гарно структурована, особливо для новачків.', CURRENT_TIMESTAMP),

    (16, 2, 'ML з Python зрозумілий навіть для початківців.', CURRENT_TIMESTAMP),
    (16, 5, 'Класна подача, багато прикладів з реальних задач.', CURRENT_TIMESTAMP),

    (17, 1, 'AI: A Modern Approach – біблія для всіх, хто цікавиться штучним інтелектом.', CURRENT_TIMESTAMP),
    (17, 3, 'Великий обсяг інформації, але дуже корисно для глибокого розуміння.', CURRENT_TIMESTAMP),

    (18, 2, 'Deep Learning – дуже детальна книга, багато прикладів та концепцій.', CURRENT_TIMESTAMP),
    (18, 4, 'Підійде як для студентів, так і для досвідчених практиків.', CURRENT_TIMESTAMP),

    (19, 1, 'Effective C++ містить безліч корисних порад для досвідчених програмістів.', CURRENT_TIMESTAMP),
    (19, 5, 'Реальні приклади, які допомагають уникати помилок у C++ коді.', CURRENT_TIMESTAMP),

    (20, 2, 'Programming Pearls – класика, яка досі актуальна для логіки та оптимізації коду.', CURRENT_TIMESTAMP),
    (20, 3, 'Коротко, але дуже змістовно, багато корисних патернів мислення.', CURRENT_TIMESTAMP);