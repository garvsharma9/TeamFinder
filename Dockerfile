# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar file
COPY --from=build /build/target/*-SNAPSHOT.jar /app/teamfinder-backend.jar

# Give read/write permissions so the restricted user can execute it
RUN chmod 777 /app

# Hugging Face strictly requires port 7860
EXPOSE 7860

# Force Spring Boot to start on port 7860
ENTRYPOINT ["java", "-Dserver.port=7860", "-jar", "/app/teamfinder-backend.jar"]