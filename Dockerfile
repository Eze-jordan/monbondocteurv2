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

# Fix apt (Ubuntu 24.04 / deb822 sources) + force IPv4 + HTTPS
RUN set -eux; \
    printf 'Acquire::ForceIPv4 "true";\n' > /etc/apt/apt.conf.d/99force-ipv4; \
    sed -i 's|URIs: http://|URIs: https://|g' /etc/apt/sources.list.d/ubuntu.sources; \
    apt-get update; \
    apt-get install -y --no-install-recommends libreoffice; \
    apt-get clean; \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
