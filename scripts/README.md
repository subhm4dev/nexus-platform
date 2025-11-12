# Nexus Platform Scripts

Helper scripts for building Docker images and managing services.

## Prerequisites

1. **Docker Hub Account**: You need a Docker Hub account (username: `subhm4dev`)
2. **Docker Hub Token**: Create an access token at https://hub.docker.com/settings/security
3. **Environment Variables**: Create `infrastructure/.env` file (see `.env.example`)

## Setup

1. **Create `.env` file**:
   ```bash
   cp infrastructure/.env.example infrastructure/.env
   # Edit infrastructure/.env with your values
   ```

2. **Set Docker Hub credentials** (optional, if not using .env):
   ```bash
   export DOCKERHUB_USERNAME=subhm4dev
   export DOCKERHUB_TOKEN=your-dockerhub-token
   ```

## Scripts

### `build-images.sh`

Builds and pushes Docker images to Docker Hub.

**Usage:**
```bash
# Build all services
./scripts/build-images.sh

# Build specific service
./scripts/build-images.sh iam
./scripts/build-images.sh cart
```

**Image Naming:**
- Format: `nexus-<domain>-<service>-<short-sha>-<timestamp>`
- Example: `subhm4dev/nexus-shared-iam:abc1234-20250115120000`

### `run-all.sh`

Starts all infrastructure and services.

**Usage:**
```bash
./scripts/run-all.sh
```

This will:
1. Start infrastructure (PostgreSQL, Redis, Kafka, Zookeeper)
2. Wait for infrastructure to be ready
3. Start all microservices

**Services will be available at:**
- Gateway: http://localhost:8080
- IAM: http://localhost:8081
- User Profile: http://localhost:8082
- Address: http://localhost:8083
- Payment: http://localhost:8084
- Cart: http://localhost:8091
- Catalog: http://localhost:8092
- Checkout: http://localhost:8093
- Order: http://localhost:8094
- Inventory: http://localhost:8095
- Promo: http://localhost:8096
- Search: http://localhost:8097
- Fulfilment: http://localhost:8098

### `stop-all.sh`

Stops all services and infrastructure.

**Usage:**
```bash
./scripts/stop-all.sh
```

## CI/CD

When you push to `master` or `main` branch:
1. GitHub Actions detects which services changed
2. Only changed services are built
3. Docker images are built with tag: `<short-sha>-<timestamp>`
4. Images are pushed to Docker Hub: `subhm4dev/nexus-<domain>-<service>:<tag>`

**Required GitHub Secrets:**
- `DOCKERHUB_USERNAME`: Your Docker Hub username (`subhm4dev`)
- `DOCKERHUB_TOKEN`: Your Docker Hub access token

## Using Specific Image Tags

To use a specific image tag (from CI/CD builds):

1. **Update `.env` file**:
   ```bash
   IAM_IMAGE=subhm4dev/nexus-shared-iam:abc1234-20250115120000
   USER_PROFILE_IMAGE=subhm4dev/nexus-shared-user-profile:abc1234-20250115120000
   # ... etc
   ```

2. **Or export environment variables**:
   ```bash
   export IAM_IMAGE=subhm4dev/nexus-shared-iam:abc1234-20250115120000
   ./scripts/run-all.sh
   ```

## Troubleshooting

**Images not found:**
- Make sure images are built and pushed: `./scripts/build-images.sh`
- Check Docker Hub: https://hub.docker.com/u/subhm4dev
- Verify image tags in `.env` file

**Services not starting:**
- Check infrastructure is running: `docker ps`
- Check logs: `cd infrastructure && docker-compose -f docker-compose-services.yml logs -f [service-name]`
- Verify environment variables in `.env` file

