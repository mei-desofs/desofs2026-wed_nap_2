-- V3: Add category column to games table (supports RF-11 filter by category)
ALTER TABLE games ADD COLUMN IF NOT EXISTS category VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_games_category ON games(category);
