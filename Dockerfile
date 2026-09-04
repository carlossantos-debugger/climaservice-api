# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew bootJar --no-daemon -x test

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S climaservice && adduser -S climaservice -G climaservice

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

RUN chown -R climaservice:climaservice /app

USER climaservice

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
