package com.bookapp.core.service;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;
import com.bookapp.core.port.CatalogRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Сервіс каталогу книг
 *
 * @Service - Spring анотація, яка позначає цей клас як бізнес-сервіс
 * Spring автоматично створить бін цього класу та зареєструє його в контексті
 */
@Service
public class CatalogService {
    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final CatalogRepositoryPort repository;

    /**
     * Ін'єкція залежності через конструктор (рекомендований спосіб)
     * Spring автоматично знайде бін, що реалізує CatalogRepositoryPort
     *
     * @Autowired можна не писати для конструкторів (Spring Boot 4.3+)
     */
    @Autowired
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
}