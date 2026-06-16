-- GameFile embeddable gained an id field; add it to the existing table so
-- Hibernate's generated SELECT does not fail on the remote DB (which pre-dates this field).
ALTER TABLE game_files ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();
