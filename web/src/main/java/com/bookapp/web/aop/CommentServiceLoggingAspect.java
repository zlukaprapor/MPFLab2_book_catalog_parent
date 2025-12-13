package com.bookapp.web.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP аспект для логування викликів методу delete у CommentService
 */
@Aspect
@Component
public class CommentServiceLoggingAspect {

    private static final Logger log =
            LoggerFactory.getLogger(CommentServiceLoggingAspect.class);

    /**
     * Логування виклику методу CommentService.delete(...)
     * Вимірює час виконання та логує результат/помилки
     */
    @Around("execution(* com.bookapp.core.service.CommentService.delete(..))")
    public Object logDeleteCall(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        Object[] args = pjp.getArgs();
        Long bookId = (Long) args[0];
        Long commentId = (Long) args[1];
        Object createdAt = args.length > 2 ? args[2] : null;

        log.info("🔹 Calling CommentService.delete(bookId={}, commentId={}, createdAt={})",
                bookId, commentId, createdAt);

        try {
            Object result = pjp.proceed();
            long time = System.currentTimeMillis() - start;

            log.info("✅ CommentService.delete(bookId={}, commentId={}) finished successfully in {} ms",
                    bookId, commentId, time);

            return result;

        } catch (Exception ex) {
            long time = System.currentTimeMillis() - start;

            log.warn("❌ CommentService.delete(bookId={}, commentId={}) failed in {} ms: {}",
                    bookId, commentId, time, ex.getMessage());

            throw ex; // Важливо: прокидуємо виняток далі
        }
    }

    /**
     * Додатковий аспект для логування всіх методів сервісного шару
     */
    @Around("execution(* com.bookapp.core.service..*.*(..))")
    public Object logServiceCalls(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();

        long start = System.currentTimeMillis();

        log.debug("→ {}.{}() called", className, methodName);

        try {
            Object result = pjp.proceed();
            long time = System.currentTimeMillis() - start;

            log.debug("← {}.{}() completed in {} ms", className, methodName, time);

            return result;

        } catch (Exception ex) {
            long time = System.currentTimeMillis() - start;

            log.debug("✖ {}.{}() failed in {} ms", className, methodName, time);

            throw ex;
        }
    }
}