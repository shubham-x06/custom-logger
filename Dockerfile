# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the project, skipping tests for faster deployment
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
# Extract the jar from the build stage (assuming it's named logger-cli.jar as per logger.bat)
COPY --from=build /app/target/*.jar app.jar

# Expose HTTP and gRPC ports
EXPOSE 8080
EXPOSE 9090

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
