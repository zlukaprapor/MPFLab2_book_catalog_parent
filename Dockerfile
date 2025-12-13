# Multi-stage build for Spring Boot application

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy the parent POM
COPY pom.xml .

# Copy module POMs
COPY core/pom.xml core/
COPY persistence/pom.xml persistence/
COPY web/pom.xml web/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY core/src core/src
COPY persistence/src persistence/src
COPY web/src web/src

# Build the application
RUN mvn clean package -DskipTests -pl web -am

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Copy the JAR from builder stage
COPY --from=builder /app/web/target/web-*.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application with production profile
ENTRYPOINT ["java", \
            "-Dspring.profiles.active=prod", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", \
            "app.jar"]