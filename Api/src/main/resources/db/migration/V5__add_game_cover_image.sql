-- V5: Add cover_image_url column for RAWG API enrichment (RF-12)
-- Populated automatically when rawgApiId is provided on game creation.
-- Stores the RAWG background_image URL (validated to https://media.rawg.io/).
ALTER TABLE games ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(500);
