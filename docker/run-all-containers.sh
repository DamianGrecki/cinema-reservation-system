#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

if [ ! -f "$SCRIPT_DIR/.env" ]; then
  echo "No .env found, using default.env..."
  cp "$SCRIPT_DIR/default.env" "$SCRIPT_DIR/.env"
fi

echo "Loading environment variables..."
export $(grep -v '^#' "$SCRIPT_DIR/.env" | xargs)

echo "Building application images..."
docker build -t cinema/auth-service -f "$SCRIPT_DIR/Dockerfile.auth-service" "$PROJECT_ROOT"
docker build -t cinema/cinema-service -f "$SCRIPT_DIR/Dockerfile.cinema-service" "$PROJECT_ROOT"
docker build -t cinema/mail-service -f "$SCRIPT_DIR/Dockerfile.mail-service" "$PROJECT_ROOT"
docker build -t cinema/payment-provider-mock "$PROJECT_ROOT/mocks/payment/provider"

echo "Initializing Swarm (if not already active)..."
docker swarm init 2>/dev/null || true

echo "Deploying stack..."
docker stack deploy -c "$SCRIPT_DIR/docker-stack.yml" cinema

echo "Stack running! Check status with: docker stack services cinema"
