# Use official OpenJDK 17 image
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy maven wrapper and pom.xml
COPY backend/pom.xml ./backend/
COPY backend/src ./backend/src/

# Build the application
RUN apt-get update && apt-get install -y maven
RUN mvn -f backend/pom.xml clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/backend/target/*.jar app.jar

# Expose the application port
EXPOSE 8092

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
