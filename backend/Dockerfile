# =========================================================================
# UPISmart 2.0 Backend — Multi-stage Dockerfile for AWS App Runner / ECS Fargate
# =========================================================================

# Stage 1: Build the Spring Boot application
FROM gradle:8.7-jdk17 AS builder
WORKDIR /home/gradle/project

# Copy Gradle build files and source code
COPY build.gradle.kts settings.gradle.kts* ./
COPY src ./src

# Build production executable JAR (skipping test execution during docker build)
RUN gradle bootJar --no-daemon -x test

# Stage 2: Runtime Container
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root security user
RUN groupadd -r upismart && useradd -r -g upismart upismart
USER upismart:upismart

# Copy executable jar from builder stage
COPY --from=builder /home/gradle/project/build/libs/upismart-backend-*.jar app.jar

# Production Container Defaults
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
