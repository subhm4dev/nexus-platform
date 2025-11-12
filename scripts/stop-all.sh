#!/bin/bash
set -e

echo "Stopping Nexus Platform..."

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

cd "$PROJECT_ROOT/infrastructure"

echo "Stopping services..."
docker-compose -f docker-compose-services.yml down

echo "Stopping infrastructure..."
docker-compose down

echo "✅ All services stopped!"

