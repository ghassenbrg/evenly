-- Create categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    workspace_id UUID,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    icon VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT categories_sort_order_check CHECK (sort_order >= 0)
);

-- Create foreign key for categories table
ALTER TABLE categories
    ADD CONSTRAINT categories_workspace_id_fkey
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;

-- Create unique constraint for workspace_id and slug combination
CREATE UNIQUE INDEX categories_workspace_slug_unique ON categories(workspace_id, slug);

-- Create indexes for categories table
CREATE INDEX categories_workspace_id_idx ON categories(workspace_id);
CREATE INDEX categories_is_active_idx ON categories(is_active);
CREATE INDEX categories_workspace_sort_order_idx ON categories(workspace_id, sort_order);
