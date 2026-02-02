# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copy pom and source
COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src/

# Build the application
RUN mvn -f backend/pom.xml clean package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/backend/target/*.jar app.jar

# Expose the application port
EXPOSE 8092

# Memory tuning for Render's 512MB free tier
# -Xmx384m sets the max heap size to 384MB, leaving some room for the OS and non-heap memory
ENTRYPOINT ["java", "-Xmx384m", "-Xms256m", "-jar", "app.jar"]
