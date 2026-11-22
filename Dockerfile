# syntax=docker/dockerfile:1
# A2: compilacion y ejecucion del backend actual con Java 22 y WAR ejecutable.
ARG JAVA_IMAGE=eclipse-temurin:22.0.2_9-jdk-jammy
FROM ${JAVA_IMAGE} AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Git en Windows admite CRLF. Linux requiere LF en scripts de shell.
RUN sed -i 's/\r$//' mvnw && chmod 755 mvnw
COPY src/main/ src/main/
# Las pruebas requieren sus servicios de prueba y se ejecutan antes de publicar.
# Esta etapa compila desde fuentes y nunca copia un WAR antiguo de target/.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -Dmaven.test.skip=true package

FROM ${JAVA_IMAGE} AS runtime
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 marketplace \
    && useradd --uid 10001 --gid marketplace --no-create-home --shell /usr/sbin/nologin marketplace \
    && mkdir -p /app/uploads /app/reportes \
    && chown -R marketplace:marketplace /app

WORKDIR /app
COPY --from=build --chown=marketplace:marketplace /build/target/marketplace.war /app/marketplace.war
COPY deploy/api/entrypoint.sh /usr/local/bin/marketplace-entrypoint
RUN sed -i 's/\r$//' /usr/local/bin/marketplace-entrypoint \
    && chmod 755 /usr/local/bin/marketplace-entrypoint

USER marketplace
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker \
    TZ=America/Bogota \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"

# Liveness mide la respuesta del proceso. Readiness verifica PostgreSQL y Redis.
HEALTHCHECK --interval=30s --timeout=5s --start-period=180s --retries=3 \
    CMD curl --fail --silent --show-error --connect-timeout 2 --max-time 4 http://127.0.0.1:8080/actuator/health/liveness >/dev/null || exit 1

STOPSIGNAL SIGTERM
ENTRYPOINT ["/usr/local/bin/marketplace-entrypoint"]
