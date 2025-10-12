package com.bookapp.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контролер для відображення HTML сторінок
 *
 * @Controller - позначає клас як Spring MVC контролер
 * На відміну від @RestController, методи повертають назви view (HTML сторінок)
 */
@Controller
public class WebController {

    /**
     * Головна сторінка - перенаправлення на каталог
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    /**
     * Каталог книг
     */
    @GetMapping("/books")
    public String books() {
        return "redirect:/index.html";
    }
}