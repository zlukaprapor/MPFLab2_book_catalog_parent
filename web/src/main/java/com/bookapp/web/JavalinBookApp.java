package com.bookapp.web;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Comment;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;
import com.bookapp.core.exception.BusinessException;
import com.bookapp.core.exception.ValidationException;
import com.bookapp.core.service.CatalogService;
import com.bookapp.core.service.CommentService;

import com.google.gson.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavalinBookApp {
    private static final Logger log = LoggerFactory.getLogger(JavalinBookApp.class);

    private final CatalogService catalogService;
    private final CommentService commentService;
    private final Gson gson;

    public JavalinBookApp() {
        ApplicationContext ctx = ApplicationContext.getInstance();
        this.catalogService = ctx.getCatalogService();
        this.commentService = ctx.getCommentService();

        // ВИПРАВЛЕНО: правильна серіалізація LocalDateTime
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                        return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                })
                .create();
    }

    public void start(int port) {
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.staticFiles.add("/public");
        });

        app.before(ctx -> log.info("→ {} {} from {}", ctx.method(), ctx.path(), ctx.ip()));

        app.before("/api/*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "*");
        });

        app.after(ctx -> log.info("← {} {} - Status: {}", ctx.method(), ctx.path(), ctx.status()));

        app.get("/", ctx -> ctx.redirect("/index.html"));
        app.get("/api/books", this::searchBooks);
        app.get("/api/books/{id}", this::getBookDetails);
        app.post("/api/books/{bookId}/comments", this::addComment);
        app.delete("/api/books/{bookId}/comments/{commentId}", this::deleteComment);

        app.exception(ValidationException.class, (e, ctx) -> {
            log.warn("Validation error: {}", e.getMessage());
            ctx.status(400);
            ctx.json(createErrorResponse(e.getMessage()));
        });

        app.exception(BusinessException.class, (e, ctx) -> {
            log.warn("Business error: {}", e.getMessage());
            ctx.status(409);
            ctx.json(createErrorResponse(e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unexpected error", e);
            ctx.status(500);
            ctx.json(createErrorResponse("Internal server error"));
        });

        app.error(404, ctx -> ctx.json(createErrorResponse("Resource not found")));

        app.start(port);

        log.info("==========================================");
        log.info("📚 Javalin запущено на http://localhost:{}", port);
        log.info("==========================================");
    }

    private void searchBooks(Context ctx) {
        String query = ctx.queryParam("q");
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);
        String sort = ctx.queryParamAsClass("sort", String.class).getOrDefault("title");

        log.info("Search: query={}, page={}, size={}, sort={}", query, page, size, sort);
        PageRequest pageRequest = new PageRequest(page, size, sort);
        Page<Book> result = catalogService.searchBooks(query, pageRequest);
        ctx.json(result);
    }

    private void getBookDetails(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        log.info("Book Detail: id={}", id);

        Book book = catalogService.getBookById(id);
        if (book == null) {
            throw new NotFoundResponse("Book not found");
        }

        List<Comment> comments = commentService.getCommentsByBookId(id);
        Map<String, Object> response = new HashMap<>();
        response.put("book", book);
        response.put("comments", comments);
        ctx.json(response);
    }

    private void addComment(Context ctx) {
        Long bookId = ctx.pathParamAsClass("bookId", Long.class).get();
        CommentRequest request = ctx.bodyAsClass(CommentRequest.class);
        log.info("Add comment: bookId={}, author={}", bookId, request.author);
        Comment comment = commentService.addComment(bookId, request.author, request.text);
        ctx.status(201);
        ctx.json(comment);
    }

    private void deleteComment(Context ctx) {
        Long bookId = ctx.pathParamAsClass("bookId", Long.class).get();
        Long commentId = ctx.pathParamAsClass("commentId", Long.class).get();
        log.info("Delete comment: bookId={}, commentId={}", bookId, commentId);
        commentService.deleteComment(commentId);
        ctx.status(204);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }

    static class CommentRequest {
        public String author;
        public String text;
    }

    public static void main(String[] args) {
        new JavalinBookApp().start(7000);
    }
}
