-- Create expense_participants table
CREATE TABLE expense_participants (
    expense_id UUID NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    PRIMARY KEY (expense_id, user_id)
);

-- Create foreign keys for expense_participants table
ALTER TABLE expense_participants
    ADD CONSTRAINT expense_participants_expense_id_fkey
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE;

ALTER TABLE expense_participants
    ADD CONSTRAINT expense_participants_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Create indexes for expense_participants table
CREATE INDEX expense_participants_user_id_idx ON expense_participants(user_id);
CREATE INDEX expense_participants_expense_id_idx ON expense_participants(expense_id);
