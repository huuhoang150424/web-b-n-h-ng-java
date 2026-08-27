# Stage 1: Build the Spring Boot application using Gradle
FROM amazoncorretto:17-alpine AS builder
WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src src

# Build jar file without running tests
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Create lightweight runtime container
FROM amazoncorretto:17-alpine
WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
