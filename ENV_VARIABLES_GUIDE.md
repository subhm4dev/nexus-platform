# Environment Variables Guide: Local vs Docker vs Kubernetes

## Overview

This guide explains how environment variables work in different deployment environments:
- **Local Development**: `.env.local` files (Next.js frontend)
- **Docker Compose**: Environment variables in `docker-compose.yml`
- **Kubernetes**: ConfigMaps and Secrets

---

## 1. Local Development (`.env.local`)

### How It Works

In Next.js, `.env.local` files are:
- ✅ **Loaded automatically** by Next.js at build/runtime
- ✅ **Available in browser** via `process.env.NEXT_PUBLIC_*`
- ✅ **Git-ignored** (never committed to repo)
- ✅ **Per-developer** (each dev has their own)

### Example: `client/apps/web/tinysteps-cdc/.env.local`

```bash
# Gateway URL - Backend API base URL
NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080

# Healthcare App Tenant ID
NEXT_PUBLIC_APP_TENANT_ID=your-healthcare-app-tenant-id

# Domain Code
NEXT_PUBLIC_DOMAIN_CODE=healthcare
```

### Usage in Code

```javascript
// ✅ Works in browser (NEXT_PUBLIC_* prefix)
const gatewayUrl = process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:8080';

// ❌ Won't work in browser (no NEXT_PUBLIC_ prefix)
const secretKey = process.env.SECRET_KEY; // undefined in browser
```

---

## 2. Docker Compose

### How It Works

In Docker Compose, environment variables are:
- ✅ **Set in `docker-compose.yml`** or `.env` file
- ✅ **Passed to containers** at runtime
- ✅ **Can use `${VAR:-default}` syntax** for defaults
- ✅ **Shared across services** via `.env` file

### Example: `infrastructure/docker-compose-services.yml`

```yaml
services:
  gateway:
    image: ${GATEWAY_IMAGE:-subhm4dev/nexus-shared-gateway:latest}
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ecom_db
      - SPRING_DATASOURCE_USERNAME=${POSTGRES_USER:-postgres}
      - SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD:-postgres}
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092
      - IDENTITY_SERVICE_URL=http://iam:8080
```

### Environment File: `infrastructure/.env`

```bash
# Docker Hub
DOCKERHUB_USERNAME=subhm4dev
DOCKERHUB_TOKEN=your-token

# Image Tags
GATEWAY_IMAGE=subhm4dev/nexus-shared-gateway:abc1234-20250115120000
IAM_IMAGE=subhm4dev/nexus-shared-iam:latest

# Database
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Services
IDENTITY_SERVICE_URL=http://iam:8080
GATEWAY_URL=http://gateway:8080
```

### For Frontend in Docker

**Option 1: Build-time variables (Next.js)**

```dockerfile
# Dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY . .
# Build with environment variables
ARG NEXT_PUBLIC_GATEWAY_URL
ENV NEXT_PUBLIC_GATEWAY_URL=$NEXT_PUBLIC_GATEWAY_URL
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package.json ./package.json
CMD ["npm", "start"]
```

```yaml
# docker-compose.yml
services:
  tinysteps-cdc:
    build:
      context: ./client/apps/web/tinysteps-cdc
      args:
        NEXT_PUBLIC_GATEWAY_URL: ${GATEWAY_URL:-http://gateway:8080}
    environment:
      - NODE_ENV=production
```

**Option 2: Runtime variables (requires server-side rendering)**

For runtime variables, you need to:
1. Use server-side API routes to inject variables
2. Or use a config endpoint that returns environment-specific values

```javascript
// app/api/config/route.js
export async function GET() {
  return Response.json({
    gatewayUrl: process.env.GATEWAY_URL || 'http://gateway:8080',
    // Only expose non-sensitive config
  });
}
```

---

## 3. Kubernetes

### How It Works

In Kubernetes, environment variables come from:
- ✅ **ConfigMaps**: Non-sensitive configuration
- ✅ **Secrets**: Sensitive data (encrypted at rest)
- ✅ **Environment variables**: Direct injection
- ✅ **Service discovery**: Via DNS (e.g., `http://gateway-service:8080`)

### Example: ConfigMap

```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tinysteps-cdc-config
  namespace: healthcare
data:
  NEXT_PUBLIC_GATEWAY_URL: "http://gateway-service:8080"
  NEXT_PUBLIC_DOMAIN_CODE: "healthcare"
  NODE_ENV: "production"
```

### Example: Secret

```yaml
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: tinysteps-cdc-secrets
  namespace: healthcare
type: Opaque
stringData:
  DATABASE_PASSWORD: "your-secure-password"
  API_KEY: "your-api-key"
```

### Example: Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tinysteps-cdc
  namespace: healthcare
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: tinysteps-cdc
        image: subhm4dev/nexus-tinysteps-cdc:latest
        env:
          # From ConfigMap
          - name: NEXT_PUBLIC_GATEWAY_URL
            valueFrom:
              configMapKeyRef:
                name: tinysteps-cdc-config
                key: NEXT_PUBLIC_GATEWAY_URL
          # From Secret
          - name: DATABASE_PASSWORD
            valueFrom:
              secretKeyRef:
                name: tinysteps-cdc-secrets
                key: DATABASE_PASSWORD
          # Direct value
          - name: NODE_ENV
            value: "production"
        envFrom:
          # Load all keys from ConfigMap
          - configMapRef:
              name: tinysteps-cdc-config
```

### Service Discovery in K8s

Instead of hardcoding URLs, use Kubernetes service names:

```yaml
# k8s/services.yaml
apiVersion: v1
kind: Service
metadata:
  name: gateway-service
  namespace: healthcare
spec:
  selector:
    app: gateway
  ports:
    - port: 8080
      targetPort: 8080
```

Then in your ConfigMap:

```yaml
NEXT_PUBLIC_GATEWAY_URL: "http://gateway-service.healthcare.svc.cluster.local:8080"
# Or shorter (same namespace):
NEXT_PUBLIC_GATEWAY_URL: "http://gateway-service:8080"
```

---

## 4. Best Practices

### ✅ DO

1. **Use `.env.example`** files in repo (without secrets)
   ```bash
   # .env.example
   NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080
   NEXT_PUBLIC_APP_TENANT_ID=your-tenant-id
   ```

2. **Prefix browser variables** with `NEXT_PUBLIC_*` in Next.js
   ```javascript
   NEXT_PUBLIC_GATEWAY_URL  // ✅ Available in browser
   SECRET_API_KEY           // ❌ Not available in browser
   ```

3. **Use defaults** in Docker Compose
   ```yaml
   environment:
     - GATEWAY_URL=${GATEWAY_URL:-http://gateway:8080}
   ```

4. **Separate configs by environment**
   - `.env.local` - Local development
   - `.env.production` - Production build
   - ConfigMaps - K8s non-sensitive config
   - Secrets - K8s sensitive data

5. **Use service names** in Docker/K8s (not localhost)
   ```yaml
   # Docker
   GATEWAY_URL=http://gateway:8080
   
   # K8s
   GATEWAY_URL=http://gateway-service:8080
   ```

### ❌ DON'T

1. **Never commit secrets** to git
   - Use `.gitignore` for `.env.local`
   - Use Secrets in K8s
   - Use environment variables in CI/CD

2. **Don't hardcode URLs** in code
   ```javascript
   // ❌ Bad
   const url = 'http://localhost:8080';
   
   // ✅ Good
   const url = process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:8080';
   ```

3. **Don't expose secrets** to browser
   ```javascript
   // ❌ Bad - Secret exposed in browser
   NEXT_PUBLIC_SECRET_KEY=abc123
   
   // ✅ Good - Secret only on server
   SECRET_KEY=abc123  // Only in server-side code
   ```

---

## 5. Migration Path: Local → Docker → K8s

### Step 1: Local Development
```bash
# .env.local (not in git)
NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080
```

### Step 2: Docker Compose
```yaml
# docker-compose.yml
services:
  frontend:
    build:
      args:
        NEXT_PUBLIC_GATEWAY_URL: ${GATEWAY_URL:-http://gateway:8080}
```

### Step 3: Kubernetes
```yaml
# ConfigMap
data:
  NEXT_PUBLIC_GATEWAY_URL: "http://gateway-service:8080"

# Deployment
env:
  - name: NEXT_PUBLIC_GATEWAY_URL
    valueFrom:
      configMapKeyRef:
        name: frontend-config
        key: NEXT_PUBLIC_GATEWAY_URL
```

---

## 6. Quick Reference

| Environment | File/Resource | How to Set | Example |
|------------|---------------|------------|---------|
| **Local** | `.env.local` | Create file manually | `NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080` |
| **Docker** | `docker-compose.yml` | `environment:` section | `- GATEWAY_URL=http://gateway:8080` |
| **Docker** | `.env` file | Key-value pairs | `GATEWAY_URL=http://gateway:8080` |
| **K8s** | ConfigMap | `kubectl create configmap` | `kubectl create configmap frontend-config --from-env-file=.env` |
| **K8s** | Secret | `kubectl create secret` | `kubectl create secret generic frontend-secrets --from-literal=key=value` |
| **K8s** | Deployment | `env:` or `envFrom:` | `envFrom: - configMapRef: name: frontend-config` |

---

## 7. Troubleshooting

### Problem: Variables not available in browser

**Solution**: Prefix with `NEXT_PUBLIC_*` in Next.js
```bash
# ❌ Won't work in browser
GATEWAY_URL=http://localhost:8080

# ✅ Works in browser
NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080
```

### Problem: Variables undefined in Docker

**Solution**: Check if variable is set in `docker-compose.yml` or `.env` file
```bash
# Check if variable exists
docker-compose config | grep GATEWAY_URL

# Set in .env file
echo "GATEWAY_URL=http://gateway:8080" >> .env
```

### Problem: Service not reachable in Docker/K8s

**Solution**: Use service names, not localhost
```yaml
# ❌ Wrong (localhost doesn't work in containers)
GATEWAY_URL=http://localhost:8080

# ✅ Correct (use service name)
GATEWAY_URL=http://gateway:8080  # Docker
GATEWAY_URL=http://gateway-service:8080  # K8s
```

---

## Summary

- **Local**: `.env.local` files (git-ignored, per-developer)
- **Docker**: Environment variables in `docker-compose.yml` or `.env` file
- **K8s**: ConfigMaps (non-sensitive) and Secrets (sensitive)
- **Always**: Use service names in Docker/K8s, not localhost
- **Next.js**: Prefix browser variables with `NEXT_PUBLIC_*`

