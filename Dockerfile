# Use an OpenJDK image with build tools
FROM openjdk:11-jdk-slim AS builder

# Set the working directory
WORKDIR /app

# Install necessary build tools (e.g., git, Maven)
RUN apt-get update && \
    apt-get install -y git maven

# Clone your Spring Boot project's source code
RUN git clone https://github.com/FarjallahAhmed/log-analysis-app-backend.git .

# Build the Spring Boot project
RUN mvn clean package

# Use a lightweight OpenJDK image for running the application
FROM openjdk:11-jre-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/your-spring-boot-app.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Command to run your Spring Boot app
CMD ["java", "-jar", "app.jar"]
