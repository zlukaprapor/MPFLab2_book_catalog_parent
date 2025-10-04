package com.bookapp.core.service;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;
import com.bookapp.core.port.CatalogRepositoryPort;

public class CatalogService {
    private final CatalogRepositoryPort repository;

    public CatalogService(CatalogRepositoryPort repository) {
        this.repository = repository;
    }

    public Page<Book> searchBooks(String query, PageRequest pageRequest) {
        return repository.findBooks(query, pageRequest);
    }

    public Book getBookById(Long id) {
        return repository.findBookById(id)
                .orElse(null);
    }
}
