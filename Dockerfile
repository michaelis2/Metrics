# -------- Stage 1: Build --------
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy Maven files and source code
COPY pom.xml .
COPY src ./src

# Build the JAR (skip tests for speed)
RUN mvn clean package -DskipTests

# -------- Stage 2: Runtime --------
FROM openjdk:23-slim
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/server-0.0.1-SNAPSHOT.jar server.jar

# Set Spring Boot server port and bind address
ENV SERVER_ADDRESS=0.0.0.0
ENV SERVER_PORT=8937
ENV SERVER_UDP_PORT=4000


# Expose the port to Docker network and host
EXPOSE 8937
EXPOSE 4000

# Start the application
CMD ["java", "-jar", "server.jar"]
