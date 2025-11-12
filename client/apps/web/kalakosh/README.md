# Kalakosh - Pattachitra Art Gallery

A Next.js e-commerce application for selling authentic Pattachitra artworks from Odisha.

## Overview

Kalakosh is a treasure trove of Odisha's pride possession - Pattachitra. This application showcases and sells traditional Pattachitra paintings, handcrafted by master artisans from Raghurajpur, Puri.

## Tech Stack

- **Framework**: Next.js 16 (App Router)
- **React**: 19.2.0
- **State Management**: Zustand 4.4.0
- **Server State**: React Query 5.0.0
- **Styling**: Tailwind CSS 4
- **Animations**: Motion 12.23.24
- **UI Components**: Radix UI
- **Forms**: React Hook Form 7.47.0
- **Validation**: Zod 4.1.12

## Features

- Product catalog with filtering and search
- Shopping cart and checkout flow
- User authentication (cookie-based)
- Order management
- Admin dashboard (protected routes)
- Responsive design preserving Figma mockups
- Traditional Pattachitra design elements

## Getting Started

### Prerequisites

- Node.js 18+
- pnpm 10+

### Installation

```bash
# Install dependencies
pnpm install

# Run development server
pnpm dev:kalakosh
```

The application will be available at `http://localhost:3000` (or next available port).

## Project Structure

```
kalakosh/
├── app/                    # Next.js App Router pages
│   ├── page.js            # Home page
│   ├── shop/              # Product catalog
│   ├── products/[id]/     # Product detail
│   ├── cart/              # Shopping cart
│   ├── checkout/          # Checkout flow
│   ├── orders/            # Order history
│   ├── profile/           # User profile
│   ├── about/             # About & Artists
│   ├── blog/              # Blog & Stories
│   └── admin/             # Admin dashboard (protected)
├── components/            # React components
│   ├── Header.js          # Navigation header
│   ├── Footer.js          # Footer
│   ├── ProductCard.js     # Product card component
│   ├── AuthModal.js       # Authentication modal
│   └── ui/                # Radix UI components
├── hooks/                 # React Query hooks
│   ├── useCart.js         # Cart operations
│   ├── useCheckout.js     # Checkout flow
│   ├── useProducts.js     # Product queries
│   └── ...
├── stores/                # Zustand stores
│   └── auth-store.js      # Authentication state
├── lib/                   # Utilities
│   ├── react-query.js     # React Query provider
│   └── razorpay.js        # Razorpay integration
└── scripts/               # Database seed scripts
    ├── seed-catalog.sql   # Products and categories
    ├── seed-iam.sql       # Users and tenants
    └── seed-inventory.sql # Stock records
```

## Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080
NEXT_PUBLIC_RAZORPAY_KEY_ID=your_razorpay_key
```

## Seed Data

Run the seed scripts to populate the database with initial data:

```bash
# IAM database
psql -d ecom_iam -f scripts/seed-iam.sql

# Catalog database
psql -d ecom_catalog -f scripts/seed-catalog.sql

# Inventory database
psql -d ecom_inventory -f scripts/seed-inventory.sql
```

See `scripts/README.md` for more details.

## Authentication

The application uses cookie-based authentication. The backend sets `httpOnly` cookies containing JWT tokens. The frontend reads these cookies via API routes (`/api/auth/status` and `/api/auth/token`).

## Admin Routes

Admin routes (`/admin/*`) are protected by:
1. Next.js middleware (checks JWT cookie for ADMIN role)
2. Admin layout component (client-side check)

## Design

The application preserves the original Figma design with:
- Traditional Pattachitra color palette (indigo, terracotta, gold, ochre, ivory)
- Custom scrollbars and animations
- Pattachitra watermark patterns
- Responsive layouts

## Integration

- Uses shared `@ecom/api-client` for backend communication
- Uses shared `@ecom/shared-schemas` for validation
- Follows same checkout flow as namaste fab
- Integrates with Razorpay for payments

## Development

```bash
# Development server
pnpm dev:kalakosh

# Build for production
pnpm build

# Start production server
pnpm start
```

## License

Private project - All rights reserved

