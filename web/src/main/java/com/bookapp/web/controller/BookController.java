//package com.bookapp.web.controller;
//
//import com.bookapp.core.domain.Book;
//import com.bookapp.core.domain.Comment;
//import com.bookapp.core.domain.Page;
//import com.bookapp.core.domain.PageRequest;
//import com.bookapp.core.exception.BusinessException;
//import com.bookapp.core.exception.ValidationException;
//import com.bookapp.core.service.CatalogService;
//import com.bookapp.core.service.CommentService;
//import com.bookapp.web.ApplicationContext;
//import com.bookapp.web.dto.ErrorResponse;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonSerializer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@WebServlet(urlPatterns = {"/books", "/books/*"})
//public class BookController extends HttpServlet {
//    private static final Logger log = LoggerFactory.getLogger(BookController.class);
//
//    // Конфігурований Gson з адаптером для LocalDateTime
//    private final Gson gson = new GsonBuilder()
//            .registerTypeAdapter(LocalDateTime.class,
//                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
//                            context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
//            .create();
//
//    private CatalogService catalogService;
//    private CommentService commentService;
//
//    @Override
//    public void init() {
//        ApplicationContext ctx = ApplicationContext.getInstance();
//        this.catalogService = ctx.getCatalogService();
//        this.commentService = ctx.getCommentService();
//    }
//
//    // Решта коду без змін...
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//        try {
//            String pathInfo = req.getPathInfo();
//
//            if (pathInfo == null || pathInfo.equals("/")) {
//                handleBookList(req, resp);
//            } else {
//                handleBookDetail(req, resp, pathInfo);
//            }
//        } catch (ValidationException e) {
//            sendError(resp, 400, "Bad Request", e.getMessage());
//        } catch (Exception e) {
//            log.error("Unexpected error in GET", e);
//            sendError(resp, 500, "Internal Server Error", "An error occurred");
//        }
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//        try {
//            String pathInfo = req.getPathInfo();
//
//            if (pathInfo != null && pathInfo.matches("/\\d+/comments")) {
//                handleAddComment(req, resp, pathInfo);
//            } else {
//                sendError(resp, 404, "Not Found", "Endpoint not found");
//            }
//        } catch (ValidationException e) {
//            sendError(resp, 400, "Bad Request", e.getMessage());
//        } catch (BusinessException e) {
//            log.warn("Business error in POST: {}", e.getMessage());
//            sendError(resp, 409, "Conflict", e.getMessage());
//        } catch (Exception e) {
//            log.error("Unexpected error in POST", e);
//            sendError(resp, 500, "Internal Server Error", "An error occurred");
//        }
//    }
//
//    @Override
//    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//        try {
//            String pathInfo = req.getPathInfo();
//
//            if (pathInfo != null && pathInfo.matches("/\\d+/comments/\\d+")) {
//                handleDeleteComment(req, resp, pathInfo);
//            } else {
//                sendError(resp, 404, "Not Found", "Endpoint not found");
//            }
//        } catch (BusinessException e) {
//            log.warn("Business error in DELETE: {}", e.getMessage());
//            sendError(resp, 409, "Conflict", e.getMessage());
//        } catch (Exception e) {
//            log.error("Unexpected error in DELETE", e);
//            sendError(resp, 500, "Internal Server Error", "An error occurred");
//        }
//    }
//
//    private void handleBookList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        String query = req.getParameter("q");
//        int page = parseIntParam(req, "page", 0);
//        int size = parseIntParam(req, "size", 10);
//        String sort = req.getParameter("sort");
//
//        PageRequest pageRequest = new PageRequest(page, size, sort);
//        Page<Book> result = catalogService.searchBooks(query, pageRequest);
//
//        sendJson(resp, 200, result);
//    }
//
//    private void handleBookDetail(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
//            throws IOException {
//        Long bookId = parseBookId(pathInfo);
//        Book book = catalogService.getBookById(bookId);
//
//        if (book == null) {
//            sendError(resp, 404, "Not Found", "Book not found");
//            return;
//        }
//
//        List<Comment> comments = commentService.getCommentsByBookId(bookId);
//
//        BookDetailResponse response = new BookDetailResponse(book, comments);
//        sendJson(resp, 200, response);
//    }
//
//    private void handleAddComment(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
//            throws IOException {
//        Long bookId = parseBookId(pathInfo);
//
//        CommentRequest commentReq = gson.fromJson(req.getReader(), CommentRequest.class);
//        Comment comment = commentService.addComment(bookId, commentReq.author, commentReq.text);
//
//        sendJson(resp, 201, comment);
//    }
//
//    private void handleDeleteComment(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
//            throws IOException {
//        String[] parts = pathInfo.split("/");
//        Long commentId = Long.parseLong(parts[3]);
//
//        commentService.deleteComment(commentId);
//        resp.setStatus(204);
//    }
//
//    private Long parseBookId(String pathInfo) {
//        String[] parts = pathInfo.split("/");
//        return Long.parseLong(parts[1]);
//    }
//
//    private int parseIntParam(HttpServletRequest req, String name, int defaultValue) {
//        String value = req.getParameter(name);
//        if (value == null) return defaultValue;
//        try {
//            return Integer.parseInt(value);
//        } catch (NumberFormatException e) {
//            throw new ValidationException("Invalid " + name + " parameter");
//        }
//    }
//
//    private void sendJson(HttpServletResponse resp, int status, Object data) throws IOException {
//        resp.setStatus(status);
//        resp.setContentType("application/json");
//        resp.setCharacterEncoding("UTF-8");
//        resp.getWriter().write(gson.toJson(data));
//    }
//
//    private void sendError(HttpServletResponse resp, int status, String error, String message)
//            throws IOException {
//        if (status >= 400 && status < 500) {
//            log.warn("Client error {}: {}", status, message);
//        } else if (status >= 500) {
//            log.error("Server error {}: {}", status, message);
//        }
//
//        ErrorResponse errorResp = new ErrorResponse(status, error, message);
//        sendJson(resp, status, errorResp);
//    }
//
//    static class BookDetailResponse {
//        final Book book;
//        final List<Comment> comments;
//
//        BookDetailResponse(Book book, List<Comment> comments) {
//            this.book = book;
//            this.comments = comments;
//        }
//    }
//
//    static class CommentRequest {
//        String author;
//        String text;
//    }
//}