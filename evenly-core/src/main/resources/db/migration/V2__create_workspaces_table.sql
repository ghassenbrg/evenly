-- Create workspaces table
CREATE TABLE workspaces (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    default_split_mode VARCHAR(20) NOT NULL,
    monthly_shared_limit NUMERIC(15,2),
    is_personal BOOLEAN NOT NULL DEFAULT FALSE,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT workspaces_default_split_mode_check CHECK (default_split_mode IN ('EQUAL', 'WEIGHTED')),
    CONSTRAINT workspaces_monthly_shared_limit_check CHECK (monthly_shared_limit IS NULL OR monthly_shared_limit > 0)
);

-- Create indexes for workspaces table
CREATE INDEX workspaces_currency_idx ON workspaces(currency);
CREATE INDEX workspaces_is_personal_idx ON workspaces(is_personal);
