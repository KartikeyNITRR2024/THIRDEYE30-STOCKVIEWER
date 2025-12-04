# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build

# Set working directory
WORKDIR /app

# Copy project files into the container
COPY . .

# Build the application JAR (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:17-jre-alpine

# Copy the correct JAR file from the build stage
COPY --from=build /app/target/THIRDEYE3.0_STOCKVIEWER-0.0.1-SNAPSHOT.jar app.jar

# Expose the application's port
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
