package com.bookapp.web.controller;

import com.bookapp.core.domain.Book;
import com.bookapp.core.domain.Comment;
import com.bookapp.core.domain.Page;
import com.bookapp.core.domain.PageRequest;
import com.bookapp.core.domain.User;
import com.bookapp.core.service.CatalogService;
import com.bookapp.core.service.CommentService;
import com.bookapp.core.service.UserService;
import com.bookapp.web.dto.BookFormDto;
import com.bookapp.web.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @GetMapping
    public String listBooks(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sort", defaultValue = "title") String sort,
            Model model) {

        PageRequest pageRequest = new PageRequest(page, size, sort);
        Page<Book> bookPage = catalogService.searchBooks(query, pageRequest);

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("query", query);
        model.addAttribute("currentPage", page);
        model.addAttribute("sort", sort);

        return "books";
    }

    @GetMapping("/{id}")
    public String bookDetails(@PathVariable Long id, Model model) {
        Book book = catalogService.getBookById(id);

        if (book == null) {
            return "error/404";
        }

        List<Comment> comments = commentService.getCommentsByBookId(id);

        model.addAttribute("book", book);
        model.addAttribute("comments", comments);

        return "book-details";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new BookFormDto());
        return "book-form";
    }

    @PostMapping("/add")
    public String addBook(@ModelAttribute("book") BookFormDto bookDto, Model model) {
        try {
            Book book = new Book(
                    null,
                    bookDto.getTitle(),
                    bookDto.getAuthor(),
                    bookDto.getIsbn(),
                    bookDto.getYear()
            );

            Book savedBook = catalogService.addBook(book);

            // Відправка email повідомлення
            try {
                mailService.sendNewBookEmail(savedBook);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to send email: " + e.getMessage());
            }

            return "redirect:/books";

        } catch (Exception e) {
            model.addAttribute("error", "❌ Помилка: " + e.getMessage());
            model.addAttribute("book", bookDto);
            return "book-form";
        }
    }

    @PostMapping("/{bookId}/comments")
    public String addComment(
            @PathVariable Long bookId,
            @RequestParam String author,
            @RequestParam String text) {

        commentService.addComment(bookId, author, text);
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/{bookId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long bookId,
            @PathVariable Long commentId) {

        commentService.deleteComment(commentId);
        return "redirect:/books/" + bookId;
    }
}