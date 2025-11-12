# Docker Build and Deployment Setup

This document describes the Docker image build and deployment setup for Nexus Platform.

## Overview

- **Build Tool**: Jib Maven Plugin (no Docker daemon required)
- **Registry**: Docker Hub (`subhm4dev`)
- **Image Naming**: `nexus-<domain>-<service>-<short-sha>-<timestamp>`
- **CI/CD**: GitHub Actions (builds only changed services on push to master)

## Image Naming Convention

Images follow the pattern: `nexus-<domain>-<service>-<short-sha>-<timestamp>`

**Examples:**
- Shared services: `subhm4dev/nexus-shared-iam:abc1234-20250115120000`
- Domain services: `subhm4dev/nexus-ecommerce-cart:abc1234-20250115120000`

**Components:**
- `nexus`: Prefix for all Nexus platform images
- `<domain>`: `shared` for shared services, `ecommerce` for ecommerce domain services
- `<service>`: Service name (iam, cart, catalog, etc.)
- `<short-sha>`: First 7 characters of git commit SHA
- `<timestamp>`: Build timestamp (YYYYMMDDHHMMSS)

## Services

### Shared Services
- `nexus-shared-iam`
- `nexus-shared-user-profile`
- `nexus-shared-address`
- `nexus-shared-payment`
- `nexus-shared-gateway`

### Ecommerce Domain Services
- `nexus-ecommerce-cart`
- `nexus-ecommerce-catalog`
- `nexus-ecommerce-checkout`
- `nexus-ecommerce-order`
- `nexus-ecommerce-inventory`
- `nexus-ecommerce-promo`
- `nexus-ecommerce-search`
- `nexus-ecommerce-fulfilment`

## Local Development

### Building Images Locally

```bash
# Build all services
./scripts/build-images.sh

# Build specific service
./scripts/build-images.sh iam
./scripts/build-images.sh cart
```

**Requirements:**
- Docker Hub login: `docker login` or set `DOCKERHUB_TOKEN`
- Maven installed
- Java 25

### Running Services

```bash
# Start all services
./scripts/run-all.sh

# Stop all services
./scripts/stop-all.sh
```

**Configuration:**
- Create `infrastructure/.env` file (see `infrastructure/.env.example`)
- Set image tags to use specific versions or `latest`

## CI/CD Pipeline

### GitHub Actions Workflow

**Location**: `.github/workflows/deploy.yml`

**Triggers:**
- Push to `main` or `master` branch
- Pull requests (build only, no push)

**Process:**
1. **Change Detection**: Detects which services changed
2. **Build**: Only builds changed services (and dependencies)
3. **Docker Build**: Builds images with Jib
4. **Push**: Pushes to Docker Hub with sha-timestamp tag

**Required Secrets:**
- `DOCKERHUB_USERNAME`: Docker Hub username (`subhm4dev`)
- `DOCKERHUB_TOKEN`: Docker Hub access token

### Change Detection

The workflow detects changes at the service level:
- Individual shared services (iam, user-profile, address, payment, gateway)
- Individual domain services (cart, catalog, checkout, etc.)
- Dependencies (bom, libs) trigger dependent services

**Example:**
- Change in `shared/iam/` → Only IAM service is built
- Change in `domains/ecommerce/cart/` → Only Cart service is built
- Change in `libs/` → All services are built (dependency change)

## Docker Compose

### Infrastructure

`infrastructure/docker-compose.yml` - Infrastructure services:
- PostgreSQL
- Redis
- Kafka
- Zookeeper

### Application Services

`infrastructure/docker-compose-services.yml` - Application services:
- Uses environment variables for image names (not hardcoded)
- Supports Kubernetes migration (no hardcoded values)
- Resource limits configured (512MB per service)

**Image Configuration:**
```yaml
services:
  iam:
    image: ${IAM_IMAGE:-subhm4dev/nexus-shared-iam:latest}
```

Set `IAM_IMAGE` environment variable to use specific image tag.

## Environment Variables

### Required in `.env` file:

```bash
# Docker Hub
DOCKERHUB_USERNAME=subhm4dev
DOCKERHUB_TOKEN=your-token

# Image Tags (optional, defaults to latest)
IAM_IMAGE=subhm4dev/nexus-shared-iam:abc1234-20250115120000
USER_PROFILE_IMAGE=subhm4dev/nexus-shared-user-profile:abc1234-20250115120000
# ... etc

# Application Secrets
PASSWORD_PEPPER=your-64-byte-pepper
GITHUB_TOKEN=your-github-token
RAZORPAY_KEY_ID=your-key
RAZORPAY_KEY_SECRET=your-secret
```

## Kubernetes Ready

All image names are configured via environment variables, making it easy to migrate to Kubernetes:

1. **Docker Compose**: Uses `${SERVICE_IMAGE}` variables
2. **Kubernetes**: Can use same environment variables in ConfigMap/Secrets
3. **No Hardcoding**: All image references are dynamic

## Troubleshooting

### Images not building in CI/CD

1. Check GitHub Secrets are set:
   - `DOCKERHUB_USERNAME`
   - `DOCKERHUB_TOKEN`

2. Verify workflow runs on push to master

3. Check workflow logs for errors

### Local builds failing

1. Login to Docker Hub: `docker login`
2. Or set `DOCKERHUB_TOKEN` environment variable
3. Verify Maven can access dependencies

### Services not starting

1. Check infrastructure is running: `docker ps`
2. Verify image tags in `.env` file
3. Check service logs: `docker-compose -f infrastructure/docker-compose-services.yml logs -f [service]`

## Next Steps

1. **Set up GitHub Secrets**:
   - Go to repository Settings → Secrets and variables → Actions
   - Add `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN`

2. **Create `.env` file**:
   ```bash
   cp infrastructure/.env.example infrastructure/.env
   # Edit with your values
   ```

3. **Test locally**:
   ```bash
   ./scripts/build-images.sh iam
   ./scripts/run-all.sh
   ```

4. **Push to master**:
   - Push changes to trigger CI/CD
   - Check GitHub Actions for build status
   - Verify images in Docker Hub

