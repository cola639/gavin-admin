#!/usr/bin/env bash
# chmod +x create-env.sh
# ./create-env.sh


cat > .env <<'EOF'
# Network
NETWORK_NAME=admin

# Images
MYSQL_IMAGE=mysql:8
REDIS_IMAGE=redis:7

# Container names
MYSQL_CONTAINER=admin-mysql
REDIS_CONTAINER=admin-redis

# Ports (HOST:CONTAINER)
MYSQL_PORT=6060
MYSQL_INNER_PORT=3306
REDIS_PORT=6161
REDIS_INNER_PORT=6379

# MySQL credentials
MYSQL_ROOT_PASSWORD=123456
MYSQL_DATABASE=admin

# Optional: Redis password (enable in compose if used)
# REDIS_PASSWORD=123456
EOF

echo ".env created: $(pwd)/.env"

# Load NETWORK_NAME from .env
. ./.env

# Create network only if missing
docker network inspect "${NETWORK_NAME}" >/dev/null 2>&1 || docker network create "${NETWORK_NAME}"

# Run compose
docker compose up -d || docker-compose up -d

docker ps
