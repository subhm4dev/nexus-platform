-- Notification Service Schema Migration
-- Creates tables for notification tracking

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    notification_type VARCHAR(50) NOT NULL, -- APPOINTMENT_REMINDER, PAYMENT_LINK, REFUND, TEST_RESULT
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(50) NOT NULL, -- SMS, EMAIL, PUSH, WHATSAPP
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, SENT, FAILED
    sent_at TIMESTAMP,
    failure_reason TEXT,
    metadata JSONB, -- Additional data (appointment_id, payment_link, etc.)
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_tenant ON notifications(tenant_id, domain_id);
CREATE INDEX idx_notifications_type ON notifications(notification_type);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created ON notifications(created_at);

-- Comments
COMMENT ON TABLE notifications IS 'Healthcare-specific notifications (appointment reminders, payment links, refunds, test results)';

