-- V2: Fix status check constraints to include all enum values added after initial schema

-- games: add REJECTED status (was missing from original constraint)
ALTER TABLE games DROP CONSTRAINT IF EXISTS games_status_check;
ALTER TABLE games ADD CONSTRAINT games_status_check
    CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'REMOVED'));

-- library_entries: add REFUNDED and SUSPENDED statuses
ALTER TABLE library_entries DROP CONSTRAINT IF EXISTS library_entries_status_check;
ALTER TABLE library_entries ADD CONSTRAINT library_entries_status_check
    CHECK (status IN ('ACTIVE', 'SUSPENDED', 'REFUNDED'));

-- orders: ensure all order statuses are allowed
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check;
ALTER TABLE orders ADD CONSTRAINT orders_status_check
    CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED'));
