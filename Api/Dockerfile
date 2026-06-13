# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw
RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/*.jar app.jar

# Create storage and log directories; restrict log dir to appuser only (V16.4.2)
RUN mkdir -p /app/storage /app/logs \
    && chown -R appuser:appgroup /app \
    && chmod 750 /app/logs

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
