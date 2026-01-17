-- Add settlements table and settled flags for expenses/payments
CREATE TABLE settlements (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    start_date DATE,
    end_date DATE,
    note TEXT,
    created_by_user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    reverted_at TIMESTAMPTZ,
    reverted_by_user_id VARCHAR(100),
    CONSTRAINT settlements_date_range_check CHECK (
        start_date IS NULL OR end_date IS NULL OR start_date <= end_date
    )
);

ALTER TABLE settlements
    ADD CONSTRAINT settlements_workspace_id_fkey
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE RESTRICT;

ALTER TABLE settlements
    ADD CONSTRAINT settlements_created_by_user_id_fkey
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT;

ALTER TABLE settlements
    ADD CONSTRAINT settlements_reverted_by_user_id_fkey
    FOREIGN KEY (reverted_by_user_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX settlements_workspace_id_idx ON settlements(workspace_id);
CREATE INDEX settlements_created_at_idx ON settlements(created_at DESC);

ALTER TABLE expenses
    ADD COLUMN settlement_id UUID,
    ADD COLUMN settled_at TIMESTAMPTZ;

ALTER TABLE expenses
    ADD CONSTRAINT expenses_settlement_id_fkey
    FOREIGN KEY (settlement_id) REFERENCES settlements(id) ON DELETE SET NULL;

CREATE INDEX expenses_settlement_id_idx ON expenses(settlement_id);
CREATE INDEX expenses_unsettled_workspace_date_idx ON expenses(workspace_id, effective_date DESC)
    WHERE settlement_id IS NULL;

ALTER TABLE payments
    ADD COLUMN settlement_id UUID,
    ADD COLUMN settled_at TIMESTAMPTZ;

ALTER TABLE payments
    ADD CONSTRAINT payments_settlement_id_fkey
    FOREIGN KEY (settlement_id) REFERENCES settlements(id) ON DELETE SET NULL;

CREATE INDEX payments_settlement_id_idx ON payments(settlement_id);
CREATE INDEX payments_unsettled_workspace_date_idx ON payments(workspace_id, effective_date DESC)
    WHERE settlement_id IS NULL;

CREATE OR REPLACE FUNCTION prevent_settled_mutation() RETURNS TRIGGER AS $$
BEGIN
    IF OLD.settlement_id IS NOT NULL THEN
        IF (SELECT reverted_at FROM settlements WHERE id = OLD.settlement_id) IS NULL THEN
            RAISE EXCEPTION 'Cannot modify settled record' USING ERRCODE = '55000';
        END IF;
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER expenses_prevent_settled_update
    BEFORE UPDATE ON expenses
    FOR EACH ROW
    EXECUTE FUNCTION prevent_settled_mutation();

CREATE TRIGGER expenses_prevent_settled_delete
    BEFORE DELETE ON expenses
    FOR EACH ROW
    EXECUTE FUNCTION prevent_settled_mutation();

CREATE TRIGGER payments_prevent_settled_update
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION prevent_settled_mutation();

CREATE TRIGGER payments_prevent_settled_delete
    BEFORE DELETE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION prevent_settled_mutation();
