package com.bookapp.web.controller;

import com.bookapp.core.exception.CommentTooOldException;
import com.bookapp.core.exception.InvalidCommentDeleteException;
import com.bookapp.core.service.CommentService;
import com.bookapp.web.BookCatalogApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BookCatalogApplication.class)
@AutoConfigureMockMvc
@DisplayName("Інтеграційні тести видалення коментарів")
class CommentDeleteExceptionIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    // ================= USER =================

    @Test
    @WithMockUser(username = "ivan", roles = "USER")
    @DisplayName("Користувач: помилка 400, якщо коментар старший за 24 години")
    void користувач_отримує_400_якщо_коментар_занадто_старий() throws Exception {
        String createdAt = Instant.now().minus(25, ChronoUnit.HOURS).toString();

        doThrow(new CommentTooOldException(
                "Коментар створено більше ніж 24 години тому"))
                .when(commentService)
                .delete(anyLong(), anyLong(), any(Instant.class));

        mockMvc.perform(post("/api/comments/delete")
                        .with(csrf())
                        .param("bookId", "1")
                        .param("commentId", "2")
                        .param("createdAt", createdAt))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value(containsString("24 години")));
    }

    @Test
    @WithMockUser(username = "ivan", roles = "USER")
    @DisplayName("Користувач: успішне видалення коментаря")
    void користувач_може_успішно_видалити_коментар() throws Exception {
        String createdAt = Instant.now().minus(1, ChronoUnit.HOURS).toString();

        mockMvc.perform(post("/api/comments/delete")
                        .with(csrf())
                        .param("bookId", "1")
                        .param("commentId", "2")
                        .param("createdAt", createdAt))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ================= ADMIN =================

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Адміністратор: помилка 400 при некоректному bookId")
    void адміністратор_отримує_400_при_некоректному_bookId() throws Exception {
        String createdAt = Instant.now().toString();

        doThrow(new InvalidCommentDeleteException(
                "Invalid bookId: 0"))
                .when(commentService)
                .delete(anyLong(), anyLong(), any(Instant.class));

        mockMvc.perform(post("/api/comments/delete")
                        .with(csrf())
                        .param("bookId", "0")
                        .param("commentId", "2")
                        .param("createdAt", createdAt))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value(containsString("Invalid bookId")));
    }

    // ================= UNAUTHORIZED =================

    @Test
    @DisplayName("Неавторизований користувач: редірект на сторінку входу")
    void неавторизований_користувач_перенаправляється_на_login() throws Exception {
        mockMvc.perform(post("/api/comments/delete")
                        .with(csrf())
                        .param("bookId", "1")
                        .param("commentId", "2")
                        .param("createdAt", Instant.now().toString()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
