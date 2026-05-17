package isep.desosfs.arcadehaven.Service;

import isep.desosfs.arcadehaven.Exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enforces ASVS V6 password quality controls:
 *  V6.1.2  — context-specific term blocklist
 *  V6.2.4  — common password list
 *  V6.2.11 — blocklist enforced at registration and change
 *  V6.2.12 — HIBP k-anonymity breach check
 */
@Service
public class PasswordPolicyService {

    private static final Logger log = LoggerFactory.getLogger(PasswordPolicyService.class);

    private static final Set<String> CONTEXT_BLOCKLIST = Set.of(
            "arcadehaven", "arcade", "haven", "pixelvault", "pixel", "vault",
            "gaming", "gamer", "games", "game", "admin", "password",
            "letmein", "welcome", "qwerty", "monkey", "dragon", "master",
            "superman", "batman", "trustno", "iloveyou", "sunshine"
    );

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Value("${security.hibp.enabled:true}")
    private boolean hibpEnabled;

    private final Set<String> commonPasswords;

    public PasswordPolicyService() {
        this.commonPasswords = loadCommonPasswords();
    }

    public void validate(String password) {
        checkContextTerms(password);
        checkCommonPasswords(password);
        if (hibpEnabled) {
            checkHibp(password);
        }
    }

    private void checkContextTerms(String password) {
        String lower = password.toLowerCase();
        for (String term : CONTEXT_BLOCKLIST) {
            if (lower.contains(term)) {
                throw new BusinessException(
                        "Password contains a term that is too easy to guess. Please choose a different password.");
            }
        }
    }

    private void checkCommonPasswords(String password) {
        if (commonPasswords.contains(password.toLowerCase())) {
            throw new BusinessException(
                    "Password is too commonly used. Please choose a more unique password.");
        }
    }

    private void checkHibp(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(hashBytes).toUpperCase();
            String prefix = hex.substring(0, 5);
            String suffix = hex.substring(5);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.pwnedpasswords.com/range/" + prefix))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            for (String line : response.body().split("\r?\n")) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].trim().equalsIgnoreCase(suffix)) {
                    long count = Long.parseLong(parts[1].trim());
                    if (count > 0) {
                        throw new BusinessException(
                                "This password has appeared in known data breaches and cannot be used. "
                                        + "Please choose a different password.");
                    }
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-1 not available — cannot perform HIBP check", e);
        } catch (Exception e) {
            log.warn("HIBP breach check skipped: {}", e.getMessage());
        }
    }

    private Set<String> loadCommonPasswords() {
        try (InputStream is = PasswordPolicyService.class.getResourceAsStream("/security/common-passwords.txt")) {
            if (is == null) {
                log.warn("common-passwords.txt not found — common password check disabled");
                return Set.of();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .map(String::toLowerCase)
                        .collect(Collectors.toUnmodifiableSet());
            }
        } catch (IOException e) {
            log.error("Failed to load common-passwords.txt", e);
            return Set.of();
        }
    }
}
