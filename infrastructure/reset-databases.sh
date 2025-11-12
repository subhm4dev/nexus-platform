#!/bin/bash

# Database Reset Script
# Drops and recreates all ecommerce databases for fresh setup
# PostgreSQL is running locally on Mac (not in Docker)

set -e

# Database connection settings
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-}"

# List of all databases to manage
DATABASES=(
    "ecom_iam"
    "ecom_user_profile"
    "ecom_address"
    "ecom_payment"
    "ecom_catalog"
    "ecom_order"
    "ecom_inventory"
    "ecom_promo"
    "ecom_search"
    "ecom_fulfillment"
)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Database Reset Script${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo "This script will:"
echo "  1. Drop all ${#DATABASES[@]} databases"
echo "  2. Create all ${#DATABASES[@]} databases fresh"
echo ""
echo "Databases to reset:"
for db in "${DATABASES[@]}"; do
    echo "  - $db"
done
echo ""
echo -e "${RED}WARNING: This will DELETE ALL DATA in these databases!${NC}"
echo ""
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Operation cancelled."
    exit 0
fi

# Export password if provided (for non-interactive use)
if [ -n "$DB_PASSWORD" ]; then
    export PGPASSWORD="$DB_PASSWORD"
fi

# Function to execute SQL
execute_sql() {
    local sql="$1"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -c "$sql" || {
        echo -e "${RED}Error executing SQL: $sql${NC}"
        exit 1
    }
}

echo ""
echo -e "${YELLOW}Step 1: Dropping existing databases...${NC}"

# Drop databases (terminate connections first)
for db in "${DATABASES[@]}"; do
    echo -n "  Dropping $db... "
    
    # Terminate all connections to the database
    execute_sql "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$db' AND pid <> pg_backend_pid();" 2>/dev/null || true
    
    # Drop the database
    execute_sql "DROP DATABASE IF EXISTS $db;" 2>/dev/null && echo -e "${GREEN}✓${NC}" || echo -e "${YELLOW}⚠ (may not exist)${NC}"
done

echo ""
echo -e "${YELLOW}Step 2: Creating fresh databases...${NC}"

# Create databases
for db in "${DATABASES[@]}"; do
    echo -n "  Creating $db... "
    execute_sql "CREATE DATABASE $db;" && echo -e "${GREEN}✓${NC}" || {
        echo -e "${RED}✗${NC}"
        exit 1
    }
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}All databases reset successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Next steps:"
echo "  1. Run your services to apply migrations"
echo "  2. Or manually run Flyway migrations"
echo ""

