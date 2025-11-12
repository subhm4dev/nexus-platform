# Kalakosh Seed Data Scripts

This directory contains SQL scripts to seed the Kalakosh database with initial data.

## Scripts

- **`seed-iam.sql`**: Creates tenants, admin user, seller user, and role grants
- **`seed-catalog.sql`**: Creates categories and products for Pattachitra artworks
- **`seed-inventory.sql`**: Creates warehouse location and stock records
- **`seed-data.sql`**: Master script documenting execution order

## Execution Order

Run the scripts in this order:

1. **IAM Database** (`ecom_iam`):
   ```bash
   psql -d ecom_iam -f seed-iam.sql
   ```

2. **Catalog Database** (`ecom_catalog`):
   ```bash
   psql -d ecom_catalog -f seed-catalog.sql
   ```

3. **Inventory Database** (`ecom_inventory`):
   ```bash
   psql -d ecom_inventory -f seed-inventory.sql
   ```

## Default Credentials

### Admin User
- Email: `admin@kalakosh.art`
- Password: `Admin@123` (placeholder - use registration API in production)

### Seller User
- Email: `seller@kalakosh.art`
- Password: `Seller@123` (placeholder - use registration API in production)

## Products Seeded

The catalog seed script creates 8 products across 5 categories:
- **Mythology**: Radha Krishna Eternal Dance, Dasavatara, Krishna Leela
- **Festival**: Jagannath Rath Yatra
- **Nature**: Tree of Life, Peacock Dance
- **Deity**: Ganesha Blessing
- **Life**: Village Life

## Notes

- All products use Unsplash image URLs from the original mockData.ts
- Stock is set to 10 units per product (artworks are unique/limited)
- Tenant IDs match the pattern used in namaste fab for consistency
- In production, use the registration API to create users with proper password hashes

