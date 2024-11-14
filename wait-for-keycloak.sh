#!/bin/bash

# Ждем, пока Keycloak станет доступен
until curl -s http://keycloak:8080/auth/realms/individuals/.well-known/openid-configuration; do
  echo "Waiting for Keycloak to become available..."
  sleep 15
done

echo "Keycloak is available. Starting the application..."
exec java -jar app.jar