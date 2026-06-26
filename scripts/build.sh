#!/usr/bin/env bash
set -euo pipefail

REGISTRY="${1:-}"
TAG="${2:-latest}"
PUSH="${3:-false}"

SERVICES=(
    xueyifang-gateway
    xueyifang-auth
    xueyifang-user
    xueyifang-service
    xueyifang-trade
    xueyifang-system
    xueyifang-file
    xueyifang-message
)

cd "$(dirname "$0")/.."

# 1. Maven Build
echo "========================================"
echo " [1/2] Maven Build"
echo "========================================"
mvn clean package -DskipTests -q
echo "Maven build succeeded."

# 2. Docker Build
echo ""
echo "========================================"
echo " [2/2] Docker Build"
echo "========================================"

for svc in "${SERVICES[@]}"; do
    if [ -n "$REGISTRY" ]; then
        IMAGE_NAME="${REGISTRY}/xueyifang/${svc}:${TAG}"
    else
        IMAGE_NAME="xueyifang/${svc}:${TAG}"
    fi

    echo "Building ${IMAGE_NAME} ..."
    docker build -t "$IMAGE_NAME" -f "${svc}/Dockerfile" .

    if [ "$PUSH" = "true" ]; then
        echo "Pushing ${IMAGE_NAME} ..."
        docker push "$IMAGE_NAME"
    fi
done

echo ""
echo "All images built successfully!"
for svc in "${SERVICES[@]}"; do
    if [ -n "$REGISTRY" ]; then
        IMAGE_NAME="${REGISTRY}/xueyifang/${svc}:${TAG}"
    else
        IMAGE_NAME="xueyifang/${svc}:${TAG}"
    fi
    echo "  - ${IMAGE_NAME}"
done