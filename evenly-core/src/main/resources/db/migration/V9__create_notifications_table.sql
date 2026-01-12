-- Create notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    workspace_id UUID,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    context VARCHAR(50) NOT NULL DEFAULT 'general'
);

-- Create foreign keys for notifications table
ALTER TABLE notifications
    ADD CONSTRAINT notifications_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE notifications
    ADD CONSTRAINT notifications_workspace_id_fkey
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE SET NULL;

-- Create indexes for notifications table
CREATE INDEX notifications_user_id_idx ON notifications(user_id);
CREATE INDEX notifications_workspace_id_idx ON notifications(workspace_id);
CREATE INDEX notifications_user_read_timestamp_idx ON notifications(user_id, read, timestamp DESC);
CREATE INDEX notifications_timestamp_idx ON notifications(timestamp);
