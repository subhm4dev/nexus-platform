-- Add healthcare-specific roles to user_role enum
-- Extends existing roles: CUSTOMER, SELLER, ADMIN, STAFF, DRIVER

-- Add healthcare roles to the enum
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'DOCTOR';
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'PATIENT';
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'RECEPTIONIST';

-- Comments
COMMENT ON TYPE user_role IS 'User roles: CUSTOMER, SELLER, ADMIN, STAFF, DRIVER (ecommerce), DOCTOR, PATIENT, RECEPTIONIST (healthcare). Note: ADMIN role is shared - APP tenant ADMIN manages all branches, BRANCH tenant ADMIN manages that branch.';

