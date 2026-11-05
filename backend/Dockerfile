FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends python3 python3-venv ca-certificates \
    && python3 -m venv /opt/lizard \
    && /opt/lizard/bin/pip install --no-cache-dir lizard==1.24.3 \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home ghm
ENV PATH="/opt/lizard/bin:${PATH}"
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/cache && chown -R ghm:ghm /app
USER ghm
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
