package com.bookapp.web.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.Map;

@Component
public class EmailTemplateProcessor {

    private final Configuration cfg;

    public EmailTemplateProcessor(Configuration emailFreemarkerConfiguration) {
        this.cfg = emailFreemarkerConfiguration;
    }

    public String processTemplate(String templateName, Map<String, Object> model) {
        try {
            Template template = cfg.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot render email template " + templateName, e);
        }
    }
}