# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the project, skipping tests for faster deployment
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
ENV GROQ_API_KEY=''
# Extract the jar from the build stage (assuming it's named logger-cli.jar as per logger.bat)
COPY --from=build /app/target/logger-cli.jar app.jar

# Expose HTTP and gRPC ports
EXPOSE 8080
EXPOSE 9090

# Command to run the application
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD curl -f http://localhost:8080/ || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
