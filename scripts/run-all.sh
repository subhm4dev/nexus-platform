#!/bin/bash
set -e

echo "Starting Nexus Platform..."

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Start infrastructure
echo "Starting infrastructure (PostgreSQL, Redis, Kafka, Zookeeper)..."
cd "$PROJECT_ROOT/infrastructure"
docker-compose up -d

echo "Waiting for infrastructure to be ready..."
sleep 15

# Check if .env file exists
if [ ! -f "$PROJECT_ROOT/infrastructure/.env" ]; then
    echo "Warning: .env file not found in infrastructure directory"
    echo "Using default image tags (latest)"
    echo "Create infrastructure/.env file to specify custom image tags"
fi

# Start services
echo "Starting services..."
docker-compose -f docker-compose-services.yml up -d

echo ""
echo "✅ All services started!"
echo ""
echo "Services running on:"
echo "  - Gateway:      http://localhost:8080"
echo "  - IAM:          http://localhost:8081"
echo "  - User Profile: http://localhost:8082"
echo "  - Address:      http://localhost:8083"
echo "  - Payment:      http://localhost:8084"
echo "  - Cart:         http://localhost:8091"
echo "  - Catalog:      http://localhost:8092"
echo "  - Checkout:     http://localhost:8093"
echo "  - Order:        http://localhost:8094"
echo "  - Inventory:    http://localhost:8095"
echo "  - Promo:         http://localhost:8096"
echo "  - Search:        http://localhost:8097"
echo "  - Fulfilment:   http://localhost:8098"
echo ""
echo "Check status: docker ps"
echo "View logs: cd infrastructure && docker-compose -f docker-compose-services.yml logs -f [service-name]"

