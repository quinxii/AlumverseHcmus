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
COPY --from=builder app/target/api-gateway.jar /app/
# Command to run the Java application
CMD ["java", "-jar", "/app/api-gateway.jar"]