package com.bookapp.web.service;

import com.bookapp.core.domain.Book;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Configuration emailFreemarkerConfiguration;

    @Value("${app.mail.admin}")
    private String adminEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Відправка email про додавання нової книги
     */
    public void sendNewBookEmail(Book book) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("bookTitle", book.getTitle());
            model.put("bookAuthor", book.getAuthor());
            model.put("bookYear", book.getYear());
            model.put("bookIsbn", book.getIsbn());

            Template template = emailFreemarkerConfiguration.getTemplate("new-book.ftl");
            String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("📚 Додано нову книгу: " + book.getTitle());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully for new book: {}", book.getTitle());

        } catch (Exception e) {
            log.error("Failed to send new book email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Відправка email підтвердження реєстрації
     */
    public void sendConfirmationEmail(String toEmail, String username, String token) {
        try {
            String confirmationUrl = baseUrl + "/confirm?token=" + token;

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("confirmationUrl", confirmationUrl);

            Template template = emailFreemarkerConfiguration.getTemplate("email-confirmation.ftl");
            String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("✅ Підтвердження реєстрації в Книжковому каталозі");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Confirmation email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send confirmation email", e);
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }
}