# Stage 1: Build the application using a Gradle container
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /home/gradle/project
# Copy the entire project into the container (adjust if necessary)
COPY --chown=gradle:gradle . .
# Run the Gradle build. This will compile your Java project and trigger your frontend build.
RUN gradle clean build --no-daemon

# Stage 2: Create a lightweight runtime image using OpenJDK
FROM openjdk:17-slim
WORKDIR /app
# Copy the compiled JAR file from the builder stage.
# Adjust the path if your output JAR file location is different.
COPY --from=builder /home/gradle/project/lwg-parser-app/build/libs/*.jar app.jar
# Expose the port on which your application runs (default for Spring Boot is 8080)
EXPOSE 8080
# Specify how to run your application (this starts the Java application from the JAR)
ENTRYPOINT ["java", "-jar", "app.jar"]
