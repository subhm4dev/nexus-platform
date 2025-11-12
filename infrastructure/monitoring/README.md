# Memory Measurement Guide for Nexus Platform

This guide provides multiple methods to accurately measure RAM and memory consumption of your Nexus platform services.

## Quick Start

### 1. Measure All Services at Once

```bash
cd infrastructure/monitoring
chmod +x measure-memory.sh
./measure-memory.sh
```

This script will show:
- Docker container memory usage
- JVM memory per service (via Actuator)
- Infrastructure memory (PostgreSQL, Redis, Kafka)
- System memory summary
- Top memory consumers

### 2. Measure Individual Service in Detail

```bash
chmod +x measure-jvm-detailed.sh
./measure-jvm-detailed.sh 8081 iam-service
```

This provides detailed JVM metrics:
- Heap memory (used, committed, max)
- Non-heap memory
- Memory pools (Eden, Old Gen, Metaspace, etc.)
- Garbage collection stats
- Thread information

### 3. Continuous Monitoring

```bash
chmod +x docker-stats-continuous.sh
./docker-stats-continuous.sh 5  # Monitor every 5 seconds
```

This logs all container stats to a CSV file for analysis.

---

## Method 1: Using Spring Boot Actuator (Recommended)

Your services already have Actuator enabled with Prometheus metrics. Use these endpoints:

### Get Memory Metrics

```bash
# Heap memory used
curl http://localhost:8081/actuator/metrics/jvm.memory.used?tag=area:heap

# Heap memory max
curl http://localhost:8081/actuator/metrics/jvm.memory.max?tag=area:heap

# Non-heap memory
curl http://localhost:8081/actuator/metrics/jvm.memory.used?tag=area:nonheap

# All memory metrics
curl http://localhost:8081/actuator/metrics | jq '.names[] | select(. | contains("memory"))'
```

### Get All Metrics for a Service

```bash
curl http://localhost:8081/actuator/metrics | jq '.'
```

### Service Ports Reference

| Service | Port | Actuator URL |
|---------|------|--------------|
| Gateway | 8080 | http://localhost:8080/actuator |
| IAM | 8081 | http://localhost:8081/actuator |
| User Profile | 8082 | http://localhost:8082/actuator |
| Address | 8083 | http://localhost:8083/actuator |
| Catalog | 8084 | http://localhost:8084/actuator |
| Inventory | 8085 | http://localhost:8085/actuator |
| Promo | 8086 | http://localhost:8086/actuator |
| Cart | 8087 | http://localhost:8087/actuator |
| Checkout | 8088 | http://localhost:8088/actuator |
| Payment | 8089 | http://localhost:8089/actuator |
| Order | 8090 | http://localhost:8090/actuator |
| Fulfilment | 8091 | http://localhost:8091/actuator |
| Search | 8092 | http://localhost:8092/actuator |
| Config Server | 8888 | http://localhost:8888/actuator |

---

## Method 2: Docker Stats

### Real-time Stats

```bash
# All containers
docker stats

# Specific container
docker stats nexus-iam

# No-stream (one snapshot)
docker stats --no-stream

# Format output
docker stats --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"
```

### Container Memory Limits

```bash
# Check memory limit for a container
docker inspect nexus-iam --format='{{.HostConfig.Memory}}'

# All containers with limits
docker ps --format "{{.Names}}" | while read container; do
    limit=$(docker inspect $container --format='{{.HostConfig.Memory}}' 2>/dev/null)
    if [ "$limit" != "0" ]; then
        echo "$container: $limit bytes ($(echo "scale=2; $limit/1048576" | bc) MB)"
    fi
done
```

---

## Method 3: System-Level Monitoring

### Using `free` command

```bash
# Current memory
free -h

# Continuous monitoring
watch -n 1 free -h
```

### Using `top` or `htop`

```bash
# Sort by memory
top -o %MEM

# Or use htop (more user-friendly)
htop
```

### Using `ps` for Java processes

```bash
# All Java processes with memory
ps aux | grep java | awk '{print $2, $4, $6, $11}' | column -t

# Detailed memory per Java process
ps -eo pid,cmd,%mem,rss --sort=-%mem | grep java
```

---

## Method 4: Prometheus + Grafana (Production Ready)

Since you already have Prometheus metrics enabled, you can set up proper monitoring:

### 1. Add Prometheus to docker-compose

```yaml
prometheus:
  image: prom/prometheus:latest
  container_name: nexus-prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml
  command:
    - '--config.file=/etc/prometheus/prometheus.yml'
```

### 2. Create `prometheus.yml`

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'nexus-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'host.docker.internal:8080'  # Gateway
        - 'host.docker.internal:8081'  # IAM
        - 'host.docker.internal:8082'  # User Profile
        # ... add all services
```

### 3. Key Metrics to Monitor

- `jvm_memory_used_bytes{area="heap"}` - Heap memory used
- `jvm_memory_max_bytes{area="heap"}` - Max heap memory
- `jvm_memory_used_bytes{area="nonheap"}` - Non-heap memory
- `process_resident_memory_bytes` - Process RSS memory
- `container_memory_usage_bytes` - Container memory (if using cAdvisor)

---

## Method 5: Load Testing + Measurement

To measure memory under load:

### 1. Start all services

```bash
cd infrastructure
```

### 2. Generate load

```bash
# Using Apache Bench
ab -n 10000 -c 100 http://localhost:8080/api/v1/healthcare/doctors

# Using wrk
wrk -t12 -c400 -d30s http://localhost:8080/api/v1/healthcare/doctors
```

### 3. Measure during load

```bash
# In another terminal
./measure-memory.sh
```

---

## Method 6: JVM Memory Settings

To see what JVM settings are actually being used:

### Check JVM args

```bash
# For a running container
docker exec nexus-iam jps -v

# Or via Actuator
curl http://localhost:8081/actuator/env | jq '.propertySources[] | select(.name | contains("java"))'
```

### Set explicit memory limits

Add to your `docker-compose-services.yml`:

```yaml
services:
  iam:
    environment:
      - JAVA_OPTS=-Xmx512m -Xms256m
    mem_limit: 1g
```

Then measure again to see actual usage.

---

## Expected Memory Ranges

Based on typical Spring Boot microservices:

| Service Type | Typical Heap | Typical Total (with overhead) |
|--------------|--------------|-------------------------------|
| Lightweight (Cart, Promo) | 200-400 MB | 400-600 MB |
| Medium (IAM, Address) | 300-500 MB | 600-800 MB |
| Heavy (Catalog, Order) | 400-700 MB | 800-1200 MB |
| Infrastructure (PostgreSQL) | N/A | 2-4 GB |
| Infrastructure (Redis) | N/A | 200-500 MB |
| Infrastructure (Kafka) | N/A | 1-2 GB |

---

## Calculating Total Requirements

After measuring, calculate:

```bash
# Total JVM memory (all services)
total_jvm=$(curl -s http://localhost:8081/actuator/metrics/jvm.memory.used?tag=area:heap | jq -r '.measurements[0].value')
# ... repeat for all services and sum

# Total infrastructure
total_infra=$(docker stats --no-stream --format "{{.MemUsage}}" ecom-postgres ecom-redis ecom-kafka | ...)

# Add 20% overhead for OS and buffers
total_required=$((total_jvm + total_infra) * 1.2)
```

---

## Best Practices

1. **Measure under realistic load** - Idle memory != production memory
2. **Measure peak usage** - Run load tests and capture max memory
3. **Add 20-30% buffer** - Don't run at 100% capacity
4. **Monitor over time** - Memory usage can grow with data
5. **Set proper JVM limits** - Use `-Xmx` to prevent OOM

---

## Troubleshooting

### Service not responding to Actuator

Check if Actuator is enabled:
```bash
curl http://localhost:8081/actuator/health
```

### Metrics not available

Ensure Prometheus dependency is in `pom.xml`:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Docker stats show 0

Container might not have memory limit set. Check:
```bash
docker inspect <container> | grep -i memory
```

---

## Next Steps

1. Run `./measure-memory.sh` to get baseline
2. Run load tests and measure again
3. Set appropriate JVM memory limits
4. Set up Prometheus + Grafana for continuous monitoring
5. Use measurements to choose the right VPS size

