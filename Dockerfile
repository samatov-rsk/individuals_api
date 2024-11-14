# Dockerfile
FROM openjdk:21-jdk-slim

 #Устанавливаем curl
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY build/libs/*.jar app.jar
COPY wait-for-keycloak.sh /wait-for-keycloak.sh
RUN chmod +x /wait-for-keycloak.sh

EXPOSE 8089

ENTRYPOINT ["/wait-for-keycloak.sh"]