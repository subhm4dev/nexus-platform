#!/bin/bash

# Memory Measurement Script for Nexus Platform
# This script measures actual memory consumption of all services

echo "=========================================="
echo "Nexus Platform Memory Measurement"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to format bytes to MB/GB
format_memory() {
    local bytes=$1
    if [ $bytes -gt 1073741824 ]; then
        echo "$(echo "scale=2; $bytes/1073741824" | bc)GB"
    else
        echo "$(echo "scale=2; $bytes/1048576" | bc)MB"
    fi
}

# 1. Docker Container Memory Usage
echo -e "${GREEN}=== Docker Container Memory Usage ===${NC}"
echo ""
printf "%-30s %-15s %-15s %-15s\n" "CONTAINER" "MEM USAGE" "MEM LIMIT" "MEM %"
echo "----------------------------------------------------------------------------"

total_docker_mem=0
for container in $(docker ps --format "{{.Names}}" | grep -E "nexus-|ecom-"); do
    stats=$(docker stats --no-stream --format "{{.MemUsage}}" $container 2>/dev/null)
    if [ ! -z "$stats" ]; then
        mem_usage=$(echo $stats | awk '{print $1}')
        mem_limit=$(docker inspect $container --format='{{.HostConfig.Memory}}' 2>/dev/null)
        if [ "$mem_limit" = "0" ]; then
            mem_limit="unlimited"
        else
            mem_limit=$(format_memory $mem_limit)
        fi
        mem_percent=$(docker stats --no-stream --format "{{.MemPerc}}" $container 2>/dev/null)
        printf "%-30s %-15s %-15s %-15s\n" "$container" "$mem_usage" "$mem_limit" "$mem_percent"
        
        # Calculate total (convert to bytes for sum)
        mem_bytes=$(echo $mem_usage | sed 's/[^0-9.]//g')
        if [[ $mem_bytes =~ ^[0-9]+\.?[0-9]*$ ]]; then
            # Convert MB to bytes if needed
            if [[ $mem_usage == *"MiB"* ]]; then
                mem_bytes=$(echo "$mem_bytes * 1048576" | bc | cut -d. -f1)
            elif [[ $mem_usage == *"GiB"* ]]; then
                mem_bytes=$(echo "$mem_bytes * 1073741824" | bc | cut -d. -f1)
            fi
            total_docker_mem=$((total_docker_mem + mem_bytes))
        fi
    fi
done

echo ""
echo -e "${YELLOW}Total Docker Memory: $(format_memory $total_docker_mem)${NC}"
echo ""

# 2. JVM Memory per Service (via Actuator)
echo -e "${GREEN}=== JVM Memory per Service (via Actuator) ===${NC}"
echo ""

services=(
    "iam:8081"
    "user-profile:8082"
    "address:8083"
    "catalog:8084"
    "inventory:8085"
    "promo:8086"
    "cart:8087"
    "checkout:8088"
    "payment:8089"
    "order:8090"
    "fulfilment:8091"
    "search:8092"
    "gateway:8080"
    "config-server:8888"
)

total_jvm_heap=0
total_jvm_nonheap=0

for service_info in "${services[@]}"; do
    IFS=':' read -r service port <<< "$service_info"
    url="http://localhost:${port}/actuator/metrics/jvm.memory.used?tag=area:heap"
    
    # Try to get heap memory
    response=$(curl -s "$url" 2>/dev/null)
    if [ ! -z "$response" ] && [ "$response" != "null" ]; then
        heap_bytes=$(echo $response | grep -o '"value":[0-9.]*' | cut -d: -f2 | head -1)
        if [ ! -z "$heap_bytes" ]; then
            heap_mb=$(echo "scale=2; $heap_bytes/1048576" | bc)
            total_jvm_heap=$(echo "$total_jvm_heap + $heap_mb" | bc)
        fi
    fi
    
    # Try to get non-heap memory
    url_nonheap="http://localhost:${port}/actuator/metrics/jvm.memory.used?tag=area:nonheap"
    response_nonheap=$(curl -s "$url_nonheap" 2>/dev/null)
    if [ ! -z "$response_nonheap" ] && [ "$response_nonheap" != "null" ]; then
        nonheap_bytes=$(echo $response_nonheap | grep -o '"value":[0-9.]*' | cut -d: -f2 | head -1)
        if [ ! -z "$nonheap_bytes" ]; then
            nonheap_mb=$(echo "scale=2; $nonheap_bytes/1048576" | bc)
            total_jvm_nonheap=$(echo "$total_jvm_nonheap + $nonheap_mb" | bc)
        fi
    fi
    
    if [ ! -z "$heap_mb" ] || [ ! -z "$nonheap_mb" ]; then
        printf "%-20s Heap: %8s MB  Non-Heap: %8s MB\n" "$service" "${heap_mb:-0}" "${nonheap_mb:-0}"
    fi
done

echo ""
echo -e "${YELLOW}Total JVM Heap: ${total_jvm_heap} MB${NC}"
echo -e "${YELLOW}Total JVM Non-Heap: ${total_jvm_nonheap} MB${NC}"
echo -e "${YELLOW}Total JVM Memory: $(echo "scale=2; $total_jvm_heap + $total_jvm_nonheap" | bc) MB${NC}"
echo ""

# 3. Infrastructure Memory (PostgreSQL, Redis, Kafka)
echo -e "${GREEN}=== Infrastructure Memory Usage ===${NC}"
echo ""

infra_services=("postgres" "redis" "zookeeper" "kafka")
for infra in "${infra_services[@]}"; do
    container_name="ecom-${infra}"
    if docker ps --format "{{.Names}}" | grep -q "^${container_name}$"; then
        stats=$(docker stats --no-stream --format "{{.MemUsage}}" $container_name 2>/dev/null)
        if [ ! -z "$stats" ]; then
            printf "%-20s %s\n" "$infra" "$stats"
        fi
    fi
done

echo ""

# 4. System Memory Summary
echo -e "${GREEN}=== System Memory Summary ===${NC}"
echo ""
echo "Total System RAM:"
free -h | grep "Mem:" | awk '{print $2}'
echo ""
echo "Used System RAM:"
free -h | grep "Mem:" | awk '{print $3}'
echo ""
echo "Available System RAM:"
free -h | grep "Mem:" | awk '{print $7}'
echo ""

# 5. Top Memory Consumers
echo -e "${GREEN}=== Top 10 Memory Consumers ===${NC}"
echo ""
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" | head -11
echo ""

echo "=========================================="
echo "Measurement Complete!"
echo "=========================================="

