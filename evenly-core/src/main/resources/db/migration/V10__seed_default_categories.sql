-- Seed global categories mirroring MockDataProvider.initializeGlobalCategories()
-- Categories are global when workspace_id is NULL.

INSERT INTO categories (id, workspace_id, name, slug, icon, color, is_active, sort_order)
VALUES
    ('11111111-1111-1111-1111-111111111111', NULL, 'Food', 'food', 'fa-solid fa-pizza-slice', '#FF6B6B', TRUE, 1),
    ('22222222-2222-2222-2222-222222222222', NULL, 'Transport', 'transport', 'fa-solid fa-car', '#4ECDC4', TRUE, 2),
    ('33333333-3333-3333-3333-333333333333', NULL, 'Shopping', 'shopping', 'fa-solid fa-cart-shopping', '#45B7D1', TRUE, 3),
    ('44444444-4444-4444-4444-444444444444', NULL, 'Entertainment', 'entertainment', 'fa-solid fa-film', '#FFA07A', TRUE, 4),
    ('55555555-5555-5555-5555-555555555555', NULL, 'Bills', 'bills', 'fa-solid fa-lightbulb', '#98D8C8', TRUE, 5),
    ('66666666-6666-6666-6666-666666666666', NULL, 'Health', 'health', 'fa-solid fa-hospital', '#F7DC6F', TRUE, 6),
    ('77777777-7777-7777-7777-777777777777', NULL, 'Travel', 'travel', 'fa-solid fa-plane', '#BB8FCE', TRUE, 7),
    ('88888888-8888-8888-8888-888888888888', NULL, 'Other', 'other', 'fa-solid fa-box', '#85C1E2', TRUE, 8)
ON CONFLICT (workspace_id, slug) DO NOTHING;
