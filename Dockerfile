# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Faz download das dependências (melhora o cache de layers)
RUN ./mvnw dependency:go-offline -B
COPY src ./src
# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Segurança: criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o JAR do estágio de build
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
