# =====================================================================
# Backend Spring Boot — build multi-stage
# =====================================================================

# ---- Stage 1 : build Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache des dépendances : copiées seules d'abord
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Puis les sources
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Stage 2 : runtime JRE léger ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utilisateur non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
