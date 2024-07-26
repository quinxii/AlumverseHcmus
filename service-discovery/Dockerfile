
# Package stage
# Use a base image that contains JDK 17 on Alpine Linux
FROM openjdk:17-jdk-slim
# Copy only the JAR and dependencies from the builder stage
WORKDIR /app
COPY ./target/service-discovery.jar /app/
# Command to run the Java application
CMD ["java", "-Xmx200M", "-Xms100M", "-XX:+UseContainerSupport", "-jar", "/app/service-discovery.jar"]