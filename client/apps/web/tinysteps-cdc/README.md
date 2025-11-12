# TinySteps CDC - Healthcare Management System

Frontend web application for TinySteps CDC healthcare management system.

## Tech Stack

- **Next.js 16.0.1** - React framework
- **React 19.2.0** - UI library
- **TanStack Query 5.0.0** - Server state management
- **Zustand 4.4.0** - Client state management
- **React Hook Form 7.47.0** - Form handling
- **Zod 4.1.12** - Schema validation
- **Tailwind CSS 4** - Styling
- **Radix UI** - Accessible component primitives
- **Lucide React** - Icons

## Setup

1. Install dependencies:
```bash
pnpm install
```

2. Create `.env.local` file:
```env
NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080
NEXT_PUBLIC_APP_TENANT_ID=<your-healthcare-app-tenant-id>
NEXT_PUBLIC_DOMAIN_CODE=healthcare
```

3. Run development server:
```bash
pnpm dev
```

## Project Structure

```
apps/web/tinysteps-cdc/
├── app/                    # Next.js app router pages
│   ├── admin/              # Admin dashboard pages
│   ├── api/                # API routes
│   ├── login/              # Login page
│   └── layout.js           # Root layout
├── components/             # React components
│   ├── admin/              # Admin-specific components
│   └── ui/                 # Reusable UI components
├── hooks/                  # React Query hooks
├── lib/                    # Utilities
├── stores/                 # Zustand stores
└── middleware.js           # Route protection
```

## Features

### Implemented
- ✅ Authentication with healthcare domain
- ✅ Admin layout with sidebar navigation
- ✅ Branch selection and management
- ✅ Dashboard with metrics
- ✅ API client with all healthcare endpoints
- ✅ React Query hooks for all entities
- ✅ Route protection middleware

### Pages
- ✅ Dashboard
- ✅ Doctors (placeholder)
- ✅ Patients (placeholder)
- ✅ Appointments (placeholder)
- ✅ Session Types (placeholder)
- ✅ Availability (placeholder)
- ✅ Time Offs (placeholder)
- ✅ Payments (POS & Manual)
- ✅ Reports

## API Integration

All healthcare APIs are available via `healthcareApi` from `@ecom/api-client`:

```javascript
import { healthcareApi } from '@ecom/api-client';

// Doctor operations
await healthcareApi.doctor.create(data);
await healthcareApi.doctor.search(params);
await healthcareApi.doctor.transfer(doctorId, targetTenantId);

// Patient operations
await healthcareApi.patient.create(data);
await healthcareApi.patient.search(params);

// Appointment operations
await healthcareApi.appointment.create(data);
await healthcareApi.appointment.getAvailableSlots(doctorId, date, sessionTypeId);

// And more...
```

## Hooks

React Query hooks are available for all entities:

```javascript
import { useDoctors, useCreateDoctor } from '@/hooks/useDoctors';
import { usePatients, useCreatePatient } from '@/hooks/usePatients';
import { useAppointments, useCreateAppointment } from '@/hooks/useAppointments';
// etc.
```

## Next Steps

To complete the implementation:

1. Implement full CRUD forms for:
   - Doctors (create, edit, view, transfer)
   - Patients (create, edit, view, transfer)
   - Appointments (create, calendar view, manage)
   - Session Types (create, edit, list)
   - Availability (bulk create, manage)
   - Time Offs (create, calendar view)

2. Add data tables with search, filters, and pagination

3. Implement payment flows:
   - POS payment processing
   - Manual payment entry
   - Cash register management

4. Add report generation UI with date filters

5. Enhance UX:
   - Loading states
   - Error handling
   - Toast notifications
   - Confirmation dialogs

## Development

The app follows the same patterns as `namaste-fab` and `kalakosh` apps in the monorepo. Refer to those apps for component examples and patterns.

