#!/usr/bin/env bash

# Script to kill all Nexus Platform services running on their configured ports
# Works on macOS and Linux
# Requires bash 4.0+ for associative arrays

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Array of ports used by Nexus services
PORTS=(
  8888  # Config Server
  8080  # Gateway
  8081  # IAM (Identity Service)
  8082  # User Profile
  8083  # Address Book
  8084  # Catalog
  8085  # Inventory
  8086  # Promo
  8087  # Cart
  8088  # Checkout
  8089  # Payment
  8090  # Order
  8091  # Fulfillment
  8092  # Search
)

# Service names for better output
declare -A SERVICE_NAMES=(
  [8888]="Config Server"
  [8080]="Gateway"
  [8081]="IAM (Identity)"
  [8082]="User Profile"
  [8083]="Address Book"
  [8084]="Catalog"
  [8085]="Inventory"
  [8086]="Promo"
  [8087]="Cart"
  [8088]="Checkout"
  [8089]="Payment"
  [8090]="Order"
  [8091]="Fulfillment"
  [8092]="Search"
)

echo -e "${YELLOW}🔍 Checking for running services on Nexus Platform ports...${NC}\n"

KILLED_COUNT=0
TOTAL_PORTS=0

# Function to kill process on a port
kill_port() {
  local port=$1
  local service_name=${SERVICE_NAMES[$port]:-"Unknown Service"}
  
  TOTAL_PORTS=$((TOTAL_PORTS + 1))
  
  # Detect OS
  OS="$(uname -s)"
  
  case "$OS" in
    Darwin*)
      # macOS - use lsof
      PID=$(lsof -ti:$port 2>/dev/null)
      ;;
    Linux*)
      # Linux - use lsof or fuser
      if command -v lsof > /dev/null 2>&1; then
        PID=$(lsof -ti:$port 2>/dev/null)
      elif command -v fuser > /dev/null 2>&1; then
        PID=$(fuser $port/tcp 2>/dev/null | awk '{print $1}')
      else
        echo -e "${RED}❌ Neither lsof nor fuser is available. Please install one.${NC}"
        return 1
      fi
      ;;
    *)
      echo -e "${RED}❌ Unsupported operating system: $OS${NC}"
      return 1
      ;;
  esac
  
  if [ -z "$PID" ]; then
    echo -e "  ${GREEN}✓${NC} Port $port ($service_name): ${GREEN}No process running${NC}"
  else
    # Kill the process
    if kill -9 $PID 2>/dev/null; then
      echo -e "  ${RED}✗${NC} Port $port ($service_name): ${RED}Killed PID $PID${NC}"
      KILLED_COUNT=$((KILLED_COUNT + 1))
    else
      echo -e "  ${YELLOW}⚠${NC} Port $port ($service_name): ${YELLOW}Failed to kill PID $PID${NC}"
    fi
  fi
}

# Kill processes on all ports
for port in "${PORTS[@]}"; do
  kill_port $port
done

echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✅ Summary:${NC}"
echo -e "   Total ports checked: $TOTAL_PORTS"
echo -e "   Processes killed: $KILLED_COUNT"
echo -e "   Ports free: $((TOTAL_PORTS - KILLED_COUNT))"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if [ $KILLED_COUNT -eq 0 ]; then
  echo -e "\n${GREEN}✨ All ports are free! No services were running.${NC}\n"
  exit 0
else
  echo -e "\n${GREEN}✨ All services have been stopped!${NC}\n"
  exit 0
fi

