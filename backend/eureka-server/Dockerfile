# Stage 1: Build
FROM gradle:8.4-jdk17 AS builder
WORKDIR /build
COPY . .
RUN gradle :eureka-server:bootJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /build/eureka-server/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]