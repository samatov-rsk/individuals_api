# Указываем базовый образ с Java
FROM eclipse-temurin:21-jdk-jammy

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем jar-файл вашего приложения в контейнер
COPY build/libs/*.jar app.jar

# Указываем переменные окружения
ENV KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/individuals
ENV KEYCLOAK_CLIENT_ID=individuals-api
ENV KEYCLOAK_CLIENT_SECRET=5N7lXXF9M56qA77Do0c0lsRcQzU3gk4v

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]

# Указываем порт, который будет использовать приложение
EXPOSE 8089
