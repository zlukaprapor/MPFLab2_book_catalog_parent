package com.bookapp.persistence.adapter;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;
import com.bookapp.core.port.CatalogRepositoryPort;
import com.bookapp.persistence.entity.BookEntity;
import com.bookapp.persistence.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CatalogRepositoryAdapter implements CatalogRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(CatalogRepositoryAdapter.class);
    private final BookRepository bookRepository;

    public CatalogRepositoryAdapter(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        log.info("CatalogRepositoryAdapter initialized");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> findBooks(String query, PageRequest pageRequest) {
        Sort sort = Sort.by(pageRequest.getSort());
        org.springframework.data.domain.PageRequest springPageRequest =
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage(),
                        pageRequest.getSize(),
                        sort
                );

        org.springframework.data.domain.Page<BookEntity> entityPage;

        if (query == null || query.trim().isEmpty()) {
            entityPage = bookRepository.findAll(springPageRequest);
        } else {
            // Простий пошук по всіх записах (можна покращити через custom query)
            List<BookEntity> allBooks = bookRepository.findAll(sort);
            String lowerQuery = query.toLowerCase();

            List<BookEntity> filtered = allBooks.stream()
                    .filter(book ->
                            book.getTitle().toLowerCase().contains(lowerQuery) ||
                                    book.getAuthor().toLowerCase().contains(lowerQuery) ||
                                    (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(lowerQuery))
                    )
                    .collect(Collectors.toList());

            int start = pageRequest.getOffset();
            int end = Math.min(start + pageRequest.getSize(), filtered.size());
            List<BookEntity> pageContent = filtered.subList(
                    Math.min(start, filtered.size()),
                    end
            );

            List<Book> books = pageContent.stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());

            return new Page<>(books, pageRequest.getPage(), pageRequest.getSize(), filtered.size());
        }

        List<Book> books = entityPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());

        return new Page<>(books, pageRequest.getPage(), pageRequest.getSize(), entityPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findBookById(Long id) {
        return bookRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional
    public Book save(Book book) {
        BookEntity entity = toEntity(book);
        BookEntity saved = bookRepository.save(entity);
        log.info("Book saved: id={}", saved.getId());
        return toDomain(saved);
    }

    private Book toDomain(BookEntity entity) {
        return new Book(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getIsbn(),
                entity.getYear()
        );
    }

    private BookEntity toEntity(Book book) {
        return new BookEntity(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getYear()
        );
    }
}