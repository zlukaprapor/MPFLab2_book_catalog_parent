# ===================================
# Multi-stage Dockerfile for Spring Boot
# ===================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy parent POM
COPY pom.xml .

# Copy all modules
COPY core/ ./core/
COPY persistence/ ./persistence/
COPY web/ ./web/

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -pl web -am

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Add metadata
LABEL maintainer="book-catalog-team"
LABEL version="1.0"
LABEL description="Book Catalog Spring Boot Application"

# Create non-root user for security
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/web/target/web-*.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]