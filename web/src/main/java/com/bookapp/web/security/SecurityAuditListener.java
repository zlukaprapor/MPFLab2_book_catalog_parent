package com.bookapp.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class SecurityAuditListener {

    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String username = auth.getName();
        String authorities = auth.getAuthorities().toString();
        String timestamp = LocalDateTime.now().format(formatter);

        auditLog.info("✅ LOGIN SUCCESS | User: {} | Roles: {} | Time: {}",
                username, authorities, timestamp);
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String reason = event.getException().getMessage();
        String timestamp = LocalDateTime.now().format(formatter);

        auditLog.warn("❌ LOGIN FAILED | User: {} | Reason: {} | Time: {}",
                username, reason, timestamp);
    }

    @EventListener
    public void onAuthorizationDenied(AuthorizationDeniedEvent event) {
        Authentication auth = event.getAuthentication().get(); // Supplier -> get()
        String username = auth != null ? auth.getName() : "Anonymous";
        String resource = event.getAuthorizationDecision().toString();
        String timestamp = LocalDateTime.now().format(formatter);

        auditLog.warn("🚫 ACCESS DENIED | User: {} | Resource: {} | Time: {}",
                username, resource, timestamp);
    }
}
