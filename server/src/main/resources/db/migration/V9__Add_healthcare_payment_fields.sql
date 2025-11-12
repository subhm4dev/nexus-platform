-- Payment Service Extension for Healthcare Domain
-- Adds healthcare-specific fields to payments table and creates new tables for POS system

-- Extend existing payments table
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_source VARCHAR(50); -- ONLINE, POS, LINK, MANUAL
ALTER TABLE payments ADD COLUMN IF NOT EXISTS appointment_id UUID;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS receipt_number VARCHAR(50);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS pos_device_id VARCHAR(100);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS cashier_user_id UUID;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS notes TEXT;

-- Create indexes for new columns
CREATE INDEX IF NOT EXISTS idx_payments_appointment ON payments(appointment_id);
CREATE INDEX IF NOT EXISTS idx_payments_receipt_number ON payments(receipt_number);
CREATE INDEX IF NOT EXISTS idx_payments_cashier ON payments(cashier_user_id);
CREATE INDEX IF NOT EXISTS idx_payments_payment_source ON payments(payment_source);

-- New payment_receipts table
CREATE TABLE payment_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    receipt_number VARCHAR(50) UNIQUE NOT NULL,
    receipt_type VARCHAR(50) NOT NULL, -- INVOICE, RECEIPT, REFUND
    generated_at TIMESTAMP NOT NULL,
    generated_by UUID NOT NULL,
    file_path VARCHAR(500), -- PDF file path
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cash Register table
CREATE TABLE cash_register (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    register_date DATE NOT NULL,
    opening_balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    cash_received DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    cash_disbursed DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    closing_balance DECIMAL(19, 2),
    opened_by UUID NOT NULL,
    closed_by UUID,
    opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    notes TEXT,
    
    CONSTRAINT unique_tenant_date UNIQUE (tenant_id, domain_id, register_date)
);

-- Indexes for payment_receipts table
CREATE INDEX idx_receipts_payment ON payment_receipts(payment_id);
CREATE INDEX idx_receipts_number ON payment_receipts(receipt_number);
CREATE INDEX idx_receipts_tenant ON payment_receipts(tenant_id, domain_id);

-- Indexes for cash_register table
CREATE INDEX idx_cash_register_tenant ON cash_register(tenant_id, domain_id);
CREATE INDEX idx_cash_register_date ON cash_register(register_date);

-- Comments
COMMENT ON COLUMN payments.payment_source IS 'Payment source: ONLINE (Razorpay), POS (Point of Sale), LINK (Payment link), MANUAL (Manual entry)';
COMMENT ON COLUMN payments.appointment_id IS 'References appointments.id in appointment-service';
COMMENT ON COLUMN payments.receipt_number IS 'Receipt number format: RCP-YYYY-MMDD-####';
COMMENT ON TABLE payment_receipts IS 'Payment receipts (PDF, thermal print, digital)';
COMMENT ON TABLE cash_register IS 'Cash register management (opening/closing balance per day)';

