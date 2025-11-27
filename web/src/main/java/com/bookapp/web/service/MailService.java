package com.bookapp.web.service;

import com.bookapp.core.domain.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateProcessor templateProcessor;

    @Value("${app.mail.admin}")
    private String adminEmail;

    public MailService(JavaMailSender mailSender, EmailTemplateProcessor templateProcessor) {
        this.mailSender = mailSender;
        this.templateProcessor = templateProcessor;
    }

    public void sendNewBookEmail(Book book) {
        Map<String, Object> model = new HashMap<>();
        model.put("id", book.getId());
        model.put("title", book.getTitle());
        model.put("author", book.getAuthor());
        model.put("year", book.getYear());
        model.put("comments", null);
        model.put(
                "createdAt",
                Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
        );

        String html = templateProcessor.processTemplate("new_book.ftl", model);
        sendHtml(adminEmail, "Нова книга в каталозі", html);
    }

    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("noreply@bookapp.local");

            mailSender.send(message);
            log.info("Email sent to {} with subject '{}'", to, subject);

        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Cannot send email", e);
        }
    }
}