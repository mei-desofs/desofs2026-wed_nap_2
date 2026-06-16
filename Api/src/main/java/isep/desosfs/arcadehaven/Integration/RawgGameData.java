package isep.desosfs.arcadehaven.Integration;

/**
 * Sanitized data returned from the RAWG API for a single game.
 * All String fields have already been stripped of HTML and truncated to their DB column limits.
 */
public record RawgGameData(
        String description,   // from description_raw (plain text)
        String category,      // first genre name
        String coverImageUrl  // background_image (validated HTTPS URL on media.rawg.io)
) {}
