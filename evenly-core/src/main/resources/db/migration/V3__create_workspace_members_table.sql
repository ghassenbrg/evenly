-- Create workspace_members table
CREATE TABLE workspace_members (
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    weight_percent NUMERIC(5,2) NOT NULL DEFAULT 100.00,
    personal_monthly_limit NUMERIC(15,2),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (workspace_id, user_id),
    CONSTRAINT workspace_members_role_check CHECK (role IN ('OWNER', 'MEMBER')),
    CONSTRAINT workspace_members_weight_percent_check CHECK (weight_percent >= 0 AND weight_percent <= 100),
    CONSTRAINT workspace_members_personal_monthly_limit_check CHECK (personal_monthly_limit IS NULL OR personal_monthly_limit > 0)
);

-- Create foreign keys for workspace_members table
ALTER TABLE workspace_members
    ADD CONSTRAINT workspace_members_workspace_id_fkey
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;

ALTER TABLE workspace_members
    ADD CONSTRAINT workspace_members_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Create indexes for workspace_members table
CREATE INDEX workspace_members_user_id_idx ON workspace_members(user_id);
CREATE INDEX workspace_members_workspace_id_idx ON workspace_members(workspace_id);
CREATE INDEX workspace_members_role_idx ON workspace_members(role);
