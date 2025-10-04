package com.bookapp.web;

import com.bookapp.core.port.CatalogRepositoryPort;
import com.bookapp.core.port.CommentRepositoryPort;
import com.bookapp.core.service.CatalogService;
import com.bookapp.core.service.CommentService;
import com.bookapp.persistence.CatalogRepository;
import com.bookapp.persistence.CommentRepository;
import com.bookapp.persistence.DatabaseConnection;

public class ApplicationContext {
    private static ApplicationContext instance;

    private final CatalogService catalogService;
    private final CommentService commentService;

    private ApplicationContext() {
        DatabaseConnection.initSchema();

        CatalogRepositoryPort catalogRepo = new CatalogRepository();
        CommentRepositoryPort commentRepo = new CommentRepository();

        this.catalogService = new CatalogService(catalogRepo);
        this.commentService = new CommentService(commentRepo);
    }

    public static synchronized ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public CatalogService getCatalogService() {
        return catalogService;
    }

    public CommentService getCommentService() {
        return commentService;
    }
}