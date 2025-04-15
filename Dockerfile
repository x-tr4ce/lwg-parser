# Stage 1: Build the application using a Gradle container
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /home/gradle/project/lwg-parser-app
# Copy the entire project into the container (adjust if necessary)
COPY --chown=gradle:gradle lwg-parser-app .

# --- Install Node.js v18 and npm ---
# Update package lists and install Node.js and npm
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs

# --- Build the Frontend ---
# Navigate to the frontend folder, install dependencies, and run the build
WORKDIR /home/gradle/project/lwg-parser-app/src/frontend_build
RUN npm install && npm run build
WORKDIR /home/gradle/project/lwg-parser-app

# Run the Gradle build. This will compile your Java project and trigger your frontend build.
RUN gradle clean build --no-daemon



# Stage 2: Create a lightweight runtime image using OpenJDK
FROM openjdk:21-slim
WORKDIR /app
# Copy the compiled JAR file from the builder stage.
# Adjust the path if your output JAR file location is different.
COPY --from=builder /home/gradle/project/lwg-parser-app/build/libs/*.jar app.jar
# Expose the port on which your application runs (default for Spring Boot is 8080)
EXPOSE 8080
# Specify how to run your application (this starts the Java application from the JAR)
ENTRYPOINT ["java", "-jar", "app.jar"]
