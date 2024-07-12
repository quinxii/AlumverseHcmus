# Package stage
# Use a base image that contains JDK 17 on Alpine Linux
FROM eclipse-temurin:17
# Copy only the JAR and dependencies from the builder stage
WORKDIR /app
COPY fcm-key.json /app
COPY gcp-key.json /app
ENV GCP_SERVICE_ACCOUNT_KEY_PATH=/app/gcp-key.json
ENV FCM_SERVICE_ACCOUNT_KEY_PATH=/app/fcm-key.json
COPY ./target/group-service.jar /app/
# Command to run the Java application
# Set JVM memory limits based on Heroku's plan
CMD ["java", "-Xmx200M", "-Xms100M", "-XX:+UseContainerSupport", "-jar", "-Dspring.profiles.active=heroku", "/app/group-service.jar"]