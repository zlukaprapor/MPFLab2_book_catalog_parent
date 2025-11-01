package com.bookapp.infrastructure;

import com.bookapp.core.port.CatalogRepositoryPort;
import com.bookapp.core.port.CommentRepositoryPort;
import com.bookapp.core.service.CatalogService;
import com.bookapp.core.service.CommentService;
import com.bookapp.persistence.CatalogRepository;
import com.bookapp.persistence.CommentRepository;
import com.bookapp.persistence.DatabaseConnection;

public class ApplicationInitializer {

    public static CatalogService createCatalogService() {
        DatabaseConnection.initSchema();
        CatalogRepositoryPort repo = new CatalogRepository();
        return new CatalogService(repo);
    }

    public static CommentService createCommentService() {
        CommentRepositoryPort repo = new CommentRepository();
        return new CommentService(repo);
    }
}
