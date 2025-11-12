#!/bin/bash
set -e

# Configuration
DOCKERHUB_USERNAME="${DOCKERHUB_USERNAME:-subhm4dev}"
DOCKERHUB_TOKEN="${DOCKERHUB_TOKEN}"

# Generate image tag: short-sha-timestamp
SHORT_SHA=$(git rev-parse --short HEAD)
TIMESTAMP=$(date +%Y%m%d%H%M%S)
IMAGE_TAG="${SHORT_SHA}-${TIMESTAMP}"

echo "Building and pushing Docker images to Docker Hub..."
echo "Username: $DOCKERHUB_USERNAME"
echo "Image Tag: $IMAGE_TAG"
echo ""

# Check if Docker Hub token is provided
if [ -z "$DOCKERHUB_TOKEN" ]; then
    echo "Warning: DOCKERHUB_TOKEN not set. Attempting to use docker login credentials..."
    if ! docker info | grep -q "Username"; then
        echo "Please login to Docker Hub first:"
        echo "  docker login"
        echo "Or set DOCKERHUB_TOKEN environment variable"
        exit 1
    fi
fi

# Login to Docker Hub if token provided
if [ -n "$DOCKERHUB_TOKEN" ]; then
    echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
fi

# Function to build and push a service
build_service() {
    local service_path=$1
    local service_name=$2
    local image_name=$3
    
    echo ""
    echo "=== Building $service_name ==="
    cd "$service_path"
    
    mvn clean compile jib:build \
        -Ddocker.image.tag="$IMAGE_TAG" \
        -Djib.to.auth.username="$DOCKERHUB_USERNAME" \
        -Djib.to.auth.password="$DOCKERHUB_TOKEN"
    
    echo "✅ Built and pushed: $DOCKERHUB_USERNAME/$image_name:$IMAGE_TAG"
    cd - > /dev/null
}

# Build shared services
if [ -z "$1" ] || [ "$1" = "iam" ]; then
    build_service "shared/iam" "IAM" "nexus-shared-iam"
fi

if [ -z "$1" ] || [ "$1" = "user-profile" ]; then
    build_service "shared/user-profile" "User Profile" "nexus-shared-user-profile"
fi

if [ -z "$1" ] || [ "$1" = "address" ]; then
    build_service "shared/address" "Address" "nexus-shared-address"
fi

if [ -z "$1" ] || [ "$1" = "payment" ]; then
    build_service "shared/payment" "Payment" "nexus-shared-payment"
fi

if [ -z "$1" ] || [ "$1" = "gateway" ]; then
    build_service "shared/gateway" "Gateway" "nexus-shared-gateway"
fi

# Build domain services
if [ -z "$1" ] || [ "$1" = "cart" ]; then
    build_service "domains/ecommerce/cart" "Cart" "nexus-ecommerce-cart"
fi

if [ -z "$1" ] || [ "$1" = "catalog" ]; then
    build_service "domains/ecommerce/catalog" "Catalog" "nexus-ecommerce-catalog"
fi

if [ -z "$1" ] || [ "$1" = "checkout" ]; then
    build_service "domains/ecommerce/checkout" "Checkout" "nexus-ecommerce-checkout"
fi

if [ -z "$1" ] || [ "$1" = "order" ]; then
    build_service "domains/ecommerce/order" "Order" "nexus-ecommerce-order"
fi

if [ -z "$1" ] || [ "$1" = "inventory" ]; then
    build_service "domains/ecommerce/inventory" "Inventory" "nexus-ecommerce-inventory"
fi

if [ -z "$1" ] || [ "$1" = "promo" ]; then
    build_service "domains/ecommerce/promo" "Promo" "nexus-ecommerce-promo"
fi

if [ -z "$1" ] || [ "$1" = "search" ]; then
    build_service "domains/ecommerce/search" "Search" "nexus-ecommerce-search"
fi

if [ -z "$1" ] || [ "$1" = "fulfilment" ]; then
    build_service "domains/ecommerce/fulfilment" "Fulfilment" "nexus-ecommerce-fulfilment"
fi

echo ""
echo "✅ All images built and pushed successfully!"
echo ""
echo "Image tag used: $IMAGE_TAG"
echo ""
echo "To use these images, set environment variables:"
echo "  export IAM_IMAGE=$DOCKERHUB_USERNAME/nexus-shared-iam:$IMAGE_TAG"
echo "  export USER_PROFILE_IMAGE=$DOCKERHUB_USERNAME/nexus-shared-user-profile:$IMAGE_TAG"
echo "  # ... etc"
echo ""
echo "Or update .env file with the image tags"

