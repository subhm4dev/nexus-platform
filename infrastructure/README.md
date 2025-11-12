# 🏗️ E-Commerce Infrastructure

Local development infrastructure setup using Docker Compose.

## What's Included

- **PostgreSQL 16** - Main database for microservices
- **Apache Kafka** - Event streaming and messaging
- **Redis 7** - Caching and session storage

## Quick Start

1. **Copy environment variables:**
   ```bash
   cp .env.example .env
   ```

2. **Start all services:**
   ```bash
   docker compose up -d
   ```

3. **Check service status:**
   ```bash
   docker compose ps
   ```

4. **View logs:**
   ```bash
   docker compose logs -f
   ```

5. **Stop all services:**
   ```bash
   docker compose down
   ```

6. **Stop and remove volumes (clean slate):**
   ```bash
   docker compose down -v
   ```

## Service Ports

| Service   | Port | URL                     |
|-----------|------|-------------------------|
| PostgreSQL| 5432 | `localhost:5432`        |
| Kafka     | 9092 | `localhost:9092`        |
| Zookeeper | 2181 | `localhost:2181`        |
| Redis     | 6379 | `localhost:6379`        |

## Connection Strings

### PostgreSQL
```
JDBC URL: jdbc:postgresql://localhost:5432/ecom_db
Username: postgres
Password: postgres
```

### Kafka
```
Bootstrap Servers: localhost:9092
```

### Redis
```
Host: localhost
Port: 6379
```

## Development Tips

- Services are on a shared network `ecom-network` - use service names (e.g., `postgres`, `kafka`) when connecting from other Docker containers
- From your host machine, use `localhost` with the ports above
- Data persists in Docker volumes - use `docker compose down -v` to start fresh

## Troubleshooting

**Port already in use:**
- Change ports in `.env` file
- Or stop the service using that port

**Services not starting:**
- Check logs: `docker compose logs <service-name>`
- Ensure Docker has enough resources allocated
- Try: `docker compose down` then `docker compose up -d`

