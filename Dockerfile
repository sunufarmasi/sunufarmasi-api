# ─── Stage 1 : Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Cache des dépendances Maven avant de copier le code source
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

# ─── Stage 2 : Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utilisateur non-root pour la sécurité
RUN addgroup -S sunufarmasi && adduser -S sunufarmasi -G sunufarmasi

COPY --from=build /app/target/*.jar app.jar
RUN chown sunufarmasi:sunufarmasi app.jar

USER sunufarmasi

EXPOSE 8080

# IMPORTANT: -Dspring.profiles.active=prod doit être AVANT -jar (JVM arg, pas programme arg)
ENTRYPOINT ["java", \
  "-Xmx512m", \
  "-Xms256m", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]