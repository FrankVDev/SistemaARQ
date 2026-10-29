# =============================================================================
# NovaSalud V3.2.1 - Dockerfile genérico para cualquier módulo del proyecto
# Uso: docker build --build-arg MODULE=novasalud-citas-api -t novasalud-citas-api .
# =============================================================================

# ---------- Etapa 1: compilación con Maven (Java 17) ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /app
COPY . .
# Si existe una configuración específica para Docker (api.properties de los
# frontends), se reemplaza la de desarrollo que apunta a localhost.
RUN if [ -d "docker/config/${MODULE}" ]; then \
        cp docker/config/${MODULE}/* ${MODULE}/src/main/resources/ ; \
    fi \
 && mvn -B -q -pl ${MODULE} -am -DskipTests package

# ---------- Etapa 2: ejecución en Tomcat 10.1 (Jakarta EE 10) ----------
FROM tomcat:10.1-jdk17-temurin
ARG MODULE
ENV TZ=America/Lima
# El nombre del .war define el context path: /novasalud-xxx
COPY --from=build /app/${MODULE}/target/${MODULE}.war /usr/local/tomcat/webapps/${MODULE}.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
