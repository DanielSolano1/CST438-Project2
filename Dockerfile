# Use a lightweight JDK base image
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Copy build output from Gradle
COPY build/libs/demo-0.0.1-SNAPSHOT.jar app.jar

# Tell Docker which port the app listens on
EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "app.jar"]