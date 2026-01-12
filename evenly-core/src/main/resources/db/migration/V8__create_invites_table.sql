-- Create invites table
CREATE TABLE invites (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    max_uses INTEGER NOT NULL,
    uses_count INTEGER NOT NULL DEFAULT 0,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT invites_max_uses_check CHECK (max_uses > 0),
    CONSTRAINT invites_uses_count_check CHECK (uses_count >= 0 AND uses_count <= max_uses),
    CONSTRAINT invites_expires_at_check CHECK (expires_at IS NULL OR expires_at > created_at)
);

-- Create foreign key for invites table
ALTER TABLE invites
    ADD CONSTRAINT invites_workspace_id_fkey
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;

-- Create indexes for invites table
CREATE INDEX invites_workspace_id_idx ON invites(workspace_id);
CREATE INDEX invites_expires_at_idx ON invites(expires_at);
