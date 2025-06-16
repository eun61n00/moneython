# 1단계: 빌드
FROM gradle:8.4-jdk17 AS builder
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle bootJar --no-daemon

# 2단계: 실행
FROM openjdk:17-jdk-alpine
LABEL authors="eunbin"
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]