#!/bin/bash

# Detailed JVM Memory Measurement Script
# Fetches detailed memory metrics from Spring Boot Actuator endpoints

SERVICE_PORT=$1
SERVICE_NAME=$2

if [ -z "$SERVICE_PORT" ] || [ -z "$SERVICE_NAME" ]; then
    echo "Usage: $0 <port> <service-name>"
    echo "Example: $0 8081 iam-service"
    exit 1
fi

BASE_URL="http://localhost:${SERVICE_PORT}/actuator"

echo "=========================================="
echo "JVM Memory Details: $SERVICE_NAME"
echo "=========================================="
echo ""

# Check if service is running
if ! curl -s "${BASE_URL}/health" > /dev/null 2>&1; then
    echo "Error: Service at port $SERVICE_PORT is not accessible"
    exit 1
fi

# Function to get metric value
get_metric() {
    local metric=$1
    local tag=$2
    local url="${BASE_URL}/metrics/${metric}"
    if [ ! -z "$tag" ]; then
        url="${url}?tag=${tag}"
    fi
    curl -s "$url" | grep -o '"value":[0-9.]*' | cut -d: -f2 | head -1
}

# Function to format bytes to MB
format_mb() {
    local bytes=$1
    echo "scale=2; $bytes/1048576" | bc
}

# Heap Memory
echo "=== Heap Memory ==="
heap_used=$(get_metric "jvm.memory.used" "area:heap")
heap_committed=$(get_metric "jvm.memory.committed" "area:heap")
heap_max=$(get_metric "jvm.memory.max" "area:heap")

if [ ! -z "$heap_used" ]; then
    echo "Used:      $(format_mb $heap_used) MB"
    echo "Committed: $(format_mb $heap_committed) MB"
    echo "Max:       $(format_mb $heap_max) MB"
    heap_percent=$(echo "scale=2; ($heap_used * 100) / $heap_max" | bc)
    echo "Usage:     ${heap_percent}%"
else
    echo "Unable to fetch heap memory metrics"
fi
echo ""

# Non-Heap Memory
echo "=== Non-Heap Memory ==="
nonheap_used=$(get_metric "jvm.memory.used" "area:nonheap")
nonheap_committed=$(get_metric "jvm.memory.committed" "area:nonheap")
nonheap_max=$(get_metric "jvm.memory.max" "area:nonheap")

if [ ! -z "$nonheap_used" ]; then
    echo "Used:      $(format_mb $nonheap_used) MB"
    echo "Committed: $(format_mb $nonheap_committed) MB"
    echo "Max:       $(format_mb $nonheap_max) MB"
else
    echo "Unable to fetch non-heap memory metrics"
fi
echo ""

# Memory Pools
echo "=== Memory Pools ==="
pools=("heap:PS_Eden_Space" "heap:PS_Old_Gen" "heap:PS_Survivor_Space" "nonheap:Metaspace" "nonheap:Compressed_Class_Space" "nonheap:Code_Cache")

for pool in "${pools[@]}"; do
    IFS=':' read -r area name <<< "$pool"
    pool_used=$(get_metric "jvm.memory.used" "area:${area},id:${name}")
    if [ ! -z "$pool_used" ] && [ "$pool_used" != "0" ]; then
        echo "$name: $(format_mb $pool_used) MB"
    fi
done
echo ""

# GC Statistics
echo "=== Garbage Collection ==="
gc_count=$(get_metric "jvm.gc.pause" "action:end_of_minor_GC,count")
gc_time=$(get_metric "jvm.gc.pause" "action:end_of_minor_GC,totalTime")

if [ ! -z "$gc_count" ]; then
    echo "Minor GC Count: $gc_count"
fi
if [ ! -z "$gc_time" ]; then
    echo "Minor GC Time:  $(echo "scale=2; $gc_time/1000" | bc) seconds"
fi
echo ""

# Process Memory (from /actuator/metrics/process.files)
echo "=== Process Info ==="
process_mem=$(get_metric "process.files.open")
if [ ! -z "$process_mem" ]; then
    echo "Open Files: $process_mem"
fi

# Thread Info
threads_live=$(get_metric "jvm.threads.live")
threads_peak=$(get_metric "jvm.threads.peak")
if [ ! -z "$threads_live" ]; then
    echo "Live Threads: $threads_live"
    echo "Peak Threads: $threads_peak"
fi
echo ""

echo "=========================================="

