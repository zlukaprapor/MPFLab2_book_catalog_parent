package com.bookapp.core.port;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;

import java.util.Optional;

public interface CatalogRepositoryPort {
    Page<Book> findBooks(String query, PageRequest pageRequest);
    Optional<Book> findBookById(Long id);
}
