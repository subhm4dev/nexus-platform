# Nexus Platform

**The Connection Point for All Your Business Domains**

Nexus is a multi-domain microservices platform that enables you to build and manage multiple business domains (ecommerce, healthcare, food delivery, travel, etc.) using shared services and infrastructure.

## Architecture

```
nexus-platform/
├── server/           # Spring Boot Modulith backend (all domains and shared services)
├── client/           # Frontend applications (Next.js web apps, React Native mobile)
├── infrastructure/   # Docker compose, Kubernetes configs
└── scripts/          # Build and deployment scripts
```

## Domains

### Ecommerce
- Cart Service
- Catalog Service
- Checkout Service
- Order Service
- Inventory Service
- Promotion Service
- Search Service
- Fulfillment Service

### Future Domains
- Hospital (appointments, prescriptions, etc.)
- Food Delivery (restaurants, delivery, etc.)
- Travel (bookings, itineraries, etc.)

## Shared Services

- **IAM** - Identity and Access Management
- **User Profile** - User profile management
- **Address** - Address book service
- **Payment** - Payment processing
- **Gateway** - API Gateway

## Quick Start

### Prerequisites
- Java 25
- Maven 3.9+
- PostgreSQL
- Redis
- Kafka

### Running Services

1. **Start Infrastructure**
   ```bash
   cd infrastructure
   docker-compose up -d
   ```

2. **Start Shared Services**
   ```bash
   # IAM
   cd shared/iam && mvn spring-boot:run
   
   # User Profile
   cd shared/user-profile && mvn spring-boot:run
   
   # Address
   cd shared/address && mvn spring-boot:run
   
   # Payment
   cd shared/payment && mvn spring-boot:run
   
   # Gateway
   cd shared/gateway && mvn spring-boot:run
   ```

3. **Start Domain Services**
   ```bash
   # Cart
   cd domains/ecommerce/cart && mvn spring-boot:run
   
   # Catalog
   cd domains/ecommerce/catalog && mvn spring-boot:run
   
   # ... etc
   ```

## Building

### Build All
```bash
mvn clean install
```

### Build Specific Module
```bash
# Build only cart service
mvn clean install -pl domains/ecommerce/cart

# Build cart and its dependencies
mvn clean install -pl domains/ecommerce/cart -am
```

## CI/CD

The CI/CD pipeline uses change detection to only build and deploy services that have changed:

- Path-based change detection
- Conditional builds per service
- Parallel builds for multiple services
- Maven dependency caching

See `.github/workflows/deploy.yml` for details.

## Documentation

- [Architecture Strategy](../ecommerce/docs/MULTI_DOMAIN_ARCHITECTURE_STRATEGY.md)
- [Testing Guide](TESTING_GUIDE.md)

## License

Proprietary

