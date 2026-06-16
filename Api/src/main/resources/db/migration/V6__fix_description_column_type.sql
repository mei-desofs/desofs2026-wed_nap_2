-- Game description was created as VARCHAR(255) in V1 but the JPA entity uses
-- columnDefinition = "TEXT". RAWG descriptions easily exceed 255 characters.
ALTER TABLE games ALTER COLUMN description TYPE TEXT;
