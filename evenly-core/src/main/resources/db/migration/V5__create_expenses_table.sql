-- Create expenses table
CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    category_id UUID,
    amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    effective_date DATE NOT NULL,
    note TEXT,
    paid_by_user_id VARCHAR(100) NOT NULL,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT expenses_amount_check CHECK (amount > 0)
);

-- Create foreign keys for expenses table
ALTER TABLE expenses
    ADD CONSTRAINT expenses_workspace_id_fkey
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE RESTRICT;

ALTER TABLE expenses
    ADD CONSTRAINT expenses_category_id_fkey
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL;

ALTER TABLE expenses
    ADD CONSTRAINT expenses_paid_by_user_id_fkey
    FOREIGN KEY (paid_by_user_id) REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE expenses
    ADD CONSTRAINT expenses_created_by_user_id_fkey
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- Create indexes for expenses table
CREATE INDEX expenses_workspace_id_idx ON expenses(workspace_id);
CREATE INDEX expenses_category_id_idx ON expenses(category_id);
CREATE INDEX expenses_paid_by_user_id_idx ON expenses(paid_by_user_id);
CREATE INDEX expenses_workspace_effective_date_idx ON expenses(workspace_id, effective_date DESC);
CREATE INDEX expenses_effective_date_idx ON expenses(effective_date);
