-- V5: Add cover_image_url column for RAWG API enrichment (RF-12)
ALTER TABLE games ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(500);
