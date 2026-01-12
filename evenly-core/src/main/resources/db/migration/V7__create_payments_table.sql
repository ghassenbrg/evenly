-- Create payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    payee_user_id UUID NOT NULL,
    paid_by_user_id UUID NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    effective_date DATE NOT NULL,
    note TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT payments_status_check CHECK (status IN ('COMPLETED', 'PENDING', 'FAILED')),
    CONSTRAINT payments_amount_check CHECK (amount > 0),
    CONSTRAINT payments_users_different_check CHECK (payee_user_id != paid_by_user_id)
);

-- Create foreign keys for payments table
ALTER TABLE payments
    ADD CONSTRAINT payments_workspace_id_fkey
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE RESTRICT;

ALTER TABLE payments
    ADD CONSTRAINT payments_payee_user_id_fkey
    FOREIGN KEY (payee_user_id) REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE payments
    ADD CONSTRAINT payments_paid_by_user_id_fkey
    FOREIGN KEY (paid_by_user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- Create indexes for payments table
CREATE INDEX payments_workspace_id_idx ON payments(workspace_id);
CREATE INDEX payments_payee_user_id_idx ON payments(payee_user_id);
CREATE INDEX payments_paid_by_user_id_idx ON payments(paid_by_user_id);
CREATE INDEX payments_status_idx ON payments(status);
CREATE INDEX payments_workspace_effective_date_idx ON payments(workspace_id, effective_date DESC);
CREATE INDEX payments_workspace_status_date_idx ON payments(workspace_id, status, effective_date DESC);
