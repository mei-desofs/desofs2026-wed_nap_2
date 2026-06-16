package isep.desosfs.arcadehaven.Integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class RawgApiClient {

    private static final Logger log = LoggerFactory.getLogger(RawgApiClient.class);

    // V13.2.4 — egress allowlist: RAWG API is the only permitted external game-metadata host.
    // URL is hardcoded (not injectable) to prevent SSRF via configuration.
    private static final String RAWG_BASE_URL = "https://api.rawg.io/api";

    // V1.1.2 / RNF-11 — strip HTML tags from any RAWG-sourced string before persisting.
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]*>");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private final RestTemplate restTemplate;

    @Value("${rawg.api.key:}")
    private String apiKey;

    public RawgApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches and sanitizes game metadata from the RAWG API.
     * Returns empty if the API key is not configured or RAWG is unavailable (RNF-28 — graceful fallback).
     */
    public Optional<RawgGameData> fetchGame(String rawgApiId) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("RAWG API key not configured — skipping enrichment for '{}'", rawgApiId);
            return Optional.empty();
        }

        // V1.2.2 — build URL with UriComponentsBuilder; never concatenate user input directly
        URI uri = UriComponentsBuilder
                .fromUriString(RAWG_BASE_URL)
                .pathSegment("games", rawgApiId)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(uri, Map.class);
            if (body == null) {
                log.warn("RAWG API returned null body for '{}'", rawgApiId);
                return Optional.empty();
            }
            return Optional.of(parse(body));
        } catch (Exception e) {
            // RNF-28 — RAWG unavailability must never fail game creation; log and continue with publisher data
            log.warn("RAWG API unavailable for '{}': {}", rawgApiId, e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private RawgGameData parse(Map<String, Object> body) {
        // description_raw is plain text; description contains HTML — prefer plain text
        String description = sanitizeText((String) body.get("description_raw"), 1000);

        // genres is an array of {id, name} objects
        String category = null;
        List<Map<String, Object>> genres = (List<Map<String, Object>>) body.get("genres");
        if (genres != null && !genres.isEmpty()) {
            category = sanitizeText((String) genres.get(0).get("name"), 100);
        }

        // background_image is a full HTTPS URL — validate it comes from RAWG's media CDN
        String coverImageUrl = sanitizeCoverImageUrl((String) body.get("background_image"));

        return new RawgGameData(description, category, coverImageUrl);
    }

    private String sanitizeText(String input, int maxLength) {
        if (input == null) return null;
        // Strip HTML tags (RNF-11, V1.1.2) and normalise whitespace
        String stripped = HTML_TAG.matcher(input).replaceAll("");
        stripped = MULTI_SPACE.matcher(stripped.strip()).replaceAll(" ");
        return stripped.isEmpty() ? null : (stripped.length() <= maxLength ? stripped : stripped.substring(0, maxLength));
    }

    private String sanitizeCoverImageUrl(String url) {
        if (url == null || url.isBlank()) return null;
        // V13.2.4 — only accept RAWG media CDN URLs over HTTPS to prevent open-redirect or SSRF via stored URL
        if (!url.startsWith("https://media.rawg.io/")) {
            log.warn("Rejected RAWG cover image URL with unexpected origin: {}", url);
            return null;
        }
        return url.length() <= 500 ? url : url.substring(0, 500);
    }
}
