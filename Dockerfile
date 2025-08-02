# --- Stage 1: Build the application ---
# Use a Maven image to build the project
FROM maven:3.8.5-openjdk-17 AS builder

# Set the working directory
WORKDIR /build

# Copy the pom.xml file to download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, creating the .jar file
RUN mvn package -DskipTests


# --- Stage 2: Create the final, lightweight image ---
# Use a lightweight Java runtime image
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the .jar file from the 'builder' stage
COPY --from=builder /build/target/authdemo-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
