# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache de dependencias: se resuelve antes de copiar el codigo fuente
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Compilar y empaquetar (los tests corren en CI, no en la imagen)
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# Usuario no-root
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /build/target/jsonblog-backend-*.jar /app/app.jar

USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
