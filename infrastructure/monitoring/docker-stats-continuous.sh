#!/bin/bash

# Continuous Docker Stats Monitoring
# Monitors all Nexus containers and logs to file

OUTPUT_FILE="memory-usage-$(date +%Y%m%d-%H%M%S).log"
INTERVAL=${1:-5}  # Default 5 seconds

echo "Starting continuous memory monitoring..."
echo "Output file: $OUTPUT_FILE"
echo "Interval: ${INTERVAL} seconds"
echo "Press Ctrl+C to stop"
echo ""

# Write header
echo "Timestamp,Container,CPU%,MemUsage,MemLimit,Mem%,NetIO,BlockIO" > $OUTPUT_FILE

# Monitor loop
while true; do
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    # Get stats for all nexus containers
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemLimit}}\t{{.MemPerc}}\t{{.NetIO}}\t{{.BlockIO}}" \
        $(docker ps --format "{{.Names}}" | grep -E "nexus-|ecom-") | \
        tail -n +2 | while read line; do
            if [ ! -z "$line" ]; then
                container=$(echo "$line" | awk '{print $1}')
                stats=$(echo "$line" | awk '{print $2","$3","$4","$5","$6","$7}')
                echo "$timestamp,$container,$stats" >> $OUTPUT_FILE
            fi
        done
    
    # Also log system memory
    sys_mem=$(free -m | grep "Mem:" | awk '{print $2","$3","$7}')
    echo "$timestamp,SYSTEM,$sys_mem" >> $OUTPUT_FILE
    
    sleep $INTERVAL
done

