# Build stage
FROM maven:3.9.8-eclipse-temurin-17 AS builder
# Set working directory
WORKDIR /app
# Copy pom.xml
COPY pom.xml .
# Install dependencies (skip tests to optimize image size)
RUN mvn dependency:go-offline -Dmaven.test.skip=true
# Copy application code
COPY src src
# Build the application (skip tests again)
RUN mvn package -Dmaven.test.skip=true

# Package stage
# Use a base image that contains JDK 17 on Alpine Linux
FROM eclipse-temurin:17
# Copy only the JAR and dependencies from the builder stage
WORKDIR /app
COPY gcp-key.json /app
ENV GCP_SERVICE_ACCOUNT_KEY_PATH=/app/gcp-key.json
COPY --from=builder app/target/auth-service.jar /app/
# Command to run the Java application
# Set JVM memory limits based on Heroku's plan
CMD ["java", "-Xmx200M", "-Xms100M", "-XX:+UseContainerSupport", "-jar", "-Dspring.profiles.active=heroku", "/app/auth-service.jar"]