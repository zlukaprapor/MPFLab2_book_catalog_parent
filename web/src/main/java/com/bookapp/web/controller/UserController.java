package com.bookapp.web.controller;

import com.bookapp.core.domain.Comment;
import com.bookapp.core.domain.User;
import com.bookapp.core.service.CommentService;
import com.bookapp.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/{id}/comments")
    public String userComments(@PathVariable Long id, Model model) {
        User user = userService.findById(id);

        if (user == null) {
            return "error/404";
        }

        List<Comment> comments = commentService.getCommentsByUserId(id);

        model.addAttribute("user", user);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("comments", comments);

        return "user-comments";
    }
}