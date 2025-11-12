#!/usr/bin/env bash

# Script to check status of all Nexus Platform services
# Works on macOS and Linux

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

echo -e "${BLUE}🔍 Checking status of all Nexus Platform services...${NC}\n"

RUNNING_COUNT=0
STOPPED_COUNT=0
TOTAL_PORTS=0

# Arrays to store running and stopped services
declare -a RUNNING_SERVICES
declare -a STOPPED_SERVICES

# Function to check process on a port
check_port() {
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
        echo -e "  ${RED}❌${NC} Port $port ($service_name): ${RED}Error - lsof/fuser not available${NC}"
        STOPPED_SERVICES+=("$port:$service_name")
        STOPPED_COUNT=$((STOPPED_COUNT + 1))
        return 1
      fi
      ;;
    *)
      echo -e "  ${RED}❌${NC} Port $port ($service_name): ${RED}Unsupported OS${NC}"
      STOPPED_SERVICES+=("$port:$service_name")
      STOPPED_COUNT=$((STOPPED_COUNT + 1))
      return 1
      ;;
  esac
  
  if [ -z "$PID" ]; then
    echo -e "  ${RED}●${NC} Port $port (${YELLOW}$service_name${NC}): ${RED}STOPPED${NC}"
    STOPPED_SERVICES+=("$port:$service_name")
    STOPPED_COUNT=$((STOPPED_COUNT + 1))
  else
    # Try to get process name
    PROCESS_NAME=$(ps -p $PID -o comm= 2>/dev/null | head -1)
    if [ -z "$PROCESS_NAME" ]; then
      PROCESS_NAME="PID $PID"
    fi
    echo -e "  ${GREEN}●${NC} Port $port (${YELLOW}$service_name${NC}): ${GREEN}RUNNING${NC} (PID: $PID, Process: $PROCESS_NAME)"
    RUNNING_SERVICES+=("$port:$service_name:$PID")
    RUNNING_COUNT=$((RUNNING_COUNT + 1))
  fi
}

# Check all ports
for port in "${PORTS[@]}"; do
  check_port $port
done

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Summary
echo -e "${BLUE}📊 Summary:${NC}"
echo -e "   Total services: $TOTAL_PORTS"
echo -e "   ${GREEN}Running: $RUNNING_COUNT${NC}"
echo -e "   ${RED}Stopped: $STOPPED_COUNT${NC}"

# Calculate percentage
if [ $TOTAL_PORTS -gt 0 ]; then
  PERCENTAGE=$((RUNNING_COUNT * 100 / TOTAL_PORTS))
  echo -e "   ${BLUE}Status: $PERCENTAGE% services running${NC}"
fi

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Detailed breakdown
if [ ${#RUNNING_SERVICES[@]} -gt 0 ]; then
  echo -e "\n${GREEN}✅ Running Services:${NC}"
  for service in "${RUNNING_SERVICES[@]}"; do
    IFS=':' read -r port name pid <<< "$service"
    echo -e "   ${GREEN}✓${NC} $name (Port: $port, PID: $pid)"
  done
fi

if [ ${#STOPPED_SERVICES[@]} -gt 0 ]; then
  echo -e "\n${RED}❌ Stopped Services:${NC}"
  for service in "${STOPPED_SERVICES[@]}"; do
    IFS=':' read -r port name <<< "$service"
    echo -e "   ${RED}✗${NC} $name (Port: $port)"
  done
fi

echo ""

# Final status message
if [ $RUNNING_COUNT -eq $TOTAL_PORTS ]; then
  echo -e "${GREEN}✨ All services are running!${NC}\n"
  exit 0
elif [ $RUNNING_COUNT -eq 0 ]; then
  echo -e "${RED}⚠️  No services are running.${NC}\n"
  exit 1
else
  echo -e "${YELLOW}⚠️  Some services are not running.${NC}\n"
  exit 1
fi

