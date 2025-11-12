package com.bookapp.core.service;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;
import com.bookapp.core.exception.ValidationException;
import com.bookapp.core.port.CatalogRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CatalogService {
    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final CatalogRepositoryPort repository;

    public CatalogService(CatalogRepositoryPort repository) {
        this.repository = repository;
        log.info("CatalogService initialized with repository: {}",
                repository.getClass().getSimpleName());
    }

    public Page<Book> searchBooks(String query, PageRequest pageRequest) {
        log.debug("Searching books with query='{}', page={}, size={}",
                query, pageRequest.getPage(), pageRequest.getSize());

        Page<Book> result = repository.findBooks(query, pageRequest);

        log.info("Found {} books (total: {})",
                result.getContent().size(), result.getTotalElements());

        return result;
    }

    public Book getBookById(Long id) {
        log.debug("Getting book by id={}", id);
        return repository.findBookById(id).orElse(null);
    }

    /**
     * Додавання нової книги
     */
    public Book addBook(Book book) {
        log.debug("Adding new book: title='{}'", book.getTitle());

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new ValidationException("Назва книги є обов'язковою");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new ValidationException("Автор книги є обов'язковим");
        }

        Book savedBook = repository.save(book);
        log.info("Book added: id={}, title='{}'", savedBook.getId(), savedBook.getTitle());

        return savedBook;
    }
}