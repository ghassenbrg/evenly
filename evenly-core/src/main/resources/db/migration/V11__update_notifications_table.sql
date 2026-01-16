-- Update notifications table to support richer notification metadata
ALTER TABLE notifications RENAME COLUMN user_id TO recipient_user_id;
ALTER TABLE notifications RENAME COLUMN content TO message;
ALTER TABLE notifications RENAME COLUMN read TO is_read;
ALTER TABLE notifications RENAME COLUMN timestamp TO created_at;
ALTER TABLE notifications RENAME COLUMN context TO entity_type;

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_user_id_fkey;

ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS actor_user_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS title VARCHAR(255),
    ADD COLUMN IF NOT EXISTS entity_id VARCHAR(100);

UPDATE notifications
SET entity_type = CASE UPPER(entity_type)
    WHEN 'EXPENSE' THEN 'EXPENSE'
    WHEN 'PAYMENT' THEN 'PAYMENT'
    WHEN 'WORKSPACE' THEN 'WORKSPACE'
    WHEN 'BUDGET' THEN 'BUDGET'
    ELSE 'WORKSPACE'
END;

UPDATE notifications
SET title = type
WHERE title IS NULL;

ALTER TABLE notifications
    ALTER COLUMN title SET NOT NULL,
    ALTER COLUMN entity_type SET NOT NULL,
    ALTER COLUMN is_read SET DEFAULT FALSE,
    ALTER COLUMN created_at SET DEFAULT NOW();

ALTER TABLE notifications
    ADD CONSTRAINT notifications_recipient_user_id_fkey
    FOREIGN KEY (recipient_user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE notifications
    ADD CONSTRAINT notifications_actor_user_id_fkey
    FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL;

DROP INDEX IF EXISTS notifications_user_id_idx;
DROP INDEX IF EXISTS notifications_user_read_timestamp_idx;
DROP INDEX IF EXISTS notifications_timestamp_idx;

CREATE INDEX notifications_recipient_user_id_idx ON notifications(recipient_user_id);
CREATE INDEX notifications_recipient_read_created_idx ON notifications(recipient_user_id, is_read, created_at DESC);
CREATE INDEX notifications_created_at_idx ON notifications(created_at);
CREATE INDEX IF NOT EXISTS notifications_workspace_id_idx ON notifications(workspace_id);
CREATE INDEX notifications_dedupe_idx ON notifications(recipient_user_id, type, entity_type, entity_id);
