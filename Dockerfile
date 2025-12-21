# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Make mvnw executable and fix line endings
RUN chmod +x mvnw
RUN sed -i 's/\r$//' mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests -Dproject.build.sourceEncoding=UTF-8

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xmx400m", "-jar", "-Dspring.profiles.active=prod", "-Dfile.encoding=UTF-8", "app.jar"]
ENTRYPOINT ["
