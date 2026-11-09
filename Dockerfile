# Etapa 1: compilar la aplicación con Maven y Java 21
FROM maven:3.9-eclipse-temurin-21 AS compilacion

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -ntp -DskipTests package


# Etapa 2: preparar la imagen para ejecutar la aplicación
FROM eclipse-temurin:21-jre

WORKDIR /app

# Crear el directorio y asignarlo a un usuario sin privilegios de administrador
RUN mkdir -p /app/datos/archivos \
    && chown -R 10001:10001 /app

COPY --from=compilacion --chown=10001:10001 /app/target/*.jar /app/app.jar

ENV BOVEDA_DIRECTORIO=/app/datos/archivos

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]