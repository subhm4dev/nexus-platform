# Kalakosh Setup Guide

## Environment Variables

Create a `.env.local` file in the `kalakosh` directory with the following:

```env
NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080
NEXT_PUBLIC_APP_TENANT_ID=371e4723-6d8c-40d2-934e-dd82a80e6541
NEXT_PUBLIC_RAZORPAY_KEY_ID=rzp_test_RdH1PD2IVUJ9xp
```

**Important:** 
- `NEXT_PUBLIC_APP_TENANT_ID` must be set to `371e4723-6d8c-40d2-934e-dd82a80e6541` (Kalakosh tenant ID)
- `NEXT_PUBLIC_GATEWAY_URL` should point to your API gateway (default: `http://localhost:8080`)

## Database Setup

Run the seed script to populate the database with products:

```bash
# IAM Database - Create seller user
psql -d ecom_iam -f scripts/seed-pattachitra-complete.sql

# Address Book Database - Create seller address
psql -d ecom_address_book -f scripts/seed-pattachitra-complete.sql

# Catalog Database - Create categories and products
psql -d ecom_catalog -f scripts/seed-pattachitra-complete.sql

# Inventory Database - Create warehouse and stock
psql -d ecom_inventory -f scripts/seed-pattachitra-complete.sql
```

**Note:** The seed script uses `gen_random_uuid()` for the seller ID, so each run will create a new seller. If you need a specific seller ID, modify the script.

## Troubleshooting

### No API calls being made

1. Check browser console for errors
2. Verify `.env.local` exists and contains `NEXT_PUBLIC_APP_TENANT_ID`
3. Restart the Next.js dev server after creating/updating `.env.local`
4. Check that `NEXT_PUBLIC_GATEWAY_URL` is correct and the gateway is running

### No products found

1. Verify the seed script was run successfully
2. Check that products were created with tenant ID `371e4723-6d8c-40d2-934e-dd82a80e6541`
3. Verify the catalog service is running and accessible
4. Check browser network tab to see if API calls are being made and what the response is

### Products not showing

1. Check browser console for API errors
2. Verify the API response format matches what the frontend expects
3. Check that products have `status: 'ACTIVE'` in the database
4. Verify the seller ID in products matches an existing seller

## Testing the Setup

1. Start the backend services (gateway, catalog, etc.)
2. Run the seed scripts
3. Start the Next.js app: `npm run dev`
4. Open the browser console and check for errors
5. Navigate to the home page - you should see featured products
6. Navigate to `/shop` - you should see all products

