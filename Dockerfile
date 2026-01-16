# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /application

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
RUN java -Djarmode=layertools -jar target/*.jar extract

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /application

RUN apt-get update && \
    apt-get install -y libreoffice && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
