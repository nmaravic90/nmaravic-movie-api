FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src src
RUN mvn -DskipTests clean package -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/nmaravic-movie-api-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/uploads/movies && \
    addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app
USER appuser

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/movies || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]

