package com.bookapp.web;

import com.bookapp.core.service.CatalogService;
import com.bookapp.core.service.CommentService;
import com.bookapp.infrastructure.ApplicationInitializer;

public class ApplicationContext {
    private static ApplicationContext instance;

    private final CatalogService catalogService;
    private final CommentService commentService;

    private ApplicationContext() {
        this.catalogService = ApplicationInitializer.createCatalogService();
        this.commentService = ApplicationInitializer.createCommentService();
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
