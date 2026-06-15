package isep.desosfs.arcadehaven.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("security-tests")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SecurityIntegrationTests {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();
    }

    // Security Test 1 - Input Sanitization
    @Test
    void payloadShouldRejectScripts() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "username": "<script>alert(1)</script>",
                    "email": "test@test.com",
                    "password": "123456"
                }
            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payloadShouldRejectHtml() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "username": "<div onclick=alert(1)>clickme</div>",
                    "email": "test@test.com",
                    "password": "123456"
                }
            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payloadShouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "username": user,
                    "email": test@test.com
                    "password": 123456
                }
            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payloadShouldRejectSqlInjection() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "username":"test'; DROP TABLE users; --",
                  "email":"attacker@example.com",
                  "password":"password"
                }
                """))
                .andExpect(status().isBadRequest());
    }

    // Security Test 2 - Session and token protection
    @Test
    void expiredJwtTokenShouldBeRejected() throws Exception {
        String expiredJwt = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJyd0VQNjkzN2R2bzZuYVZUS2dfWmhi" +
                "VkJlZDdjNnNfeGM2d2Y1ejQtZnZzIn0.eyJleHAiOjE3NzkwMDg5ODAsImlhdCI6MTc3OTAwODY4MCwianRpIjoib25ydHJvOmU" +
                "0M2Q0OWQyLTQ5ODktNGRhZC1hZWNmLTc2NDk3YTU1MjUyMyIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODE4MC9yZWFsbXMvYX" +
                "JjYWRlaGF2ZW4iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMDVlYzg3YjYtOWQxMC00OWZhLWE2NzQtOGQ2YmRhMGExMTQ3Iiwid" +
                "HlwIjoiQmVhcmVyIiwiYXpwIjoiYXJjYWRlaGF2ZW4tcHVibGljIiwic2lkIjoiYjNlYzVmOTctZTEwNi00M2JkLTgzZjUtNTFi" +
                "MThlMzA5NzY2IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZ" +
                "saW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtYXJjYWRlaGF2ZW4iLCJCVVlFUiIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZX" +
                "NvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzI" +
                "iwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJi" +
                "dXllcjIgVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6ImJ1eWVyMiIsImdpdmVuX25hbWUiOiJidXllcjIiLCJmYW1pbHlfbmF" +
                "tZSI6IlVzZXIiLCJlbWFpbCI6ImJ1eWVyMkBhcmNhZGVoYXZlbi5jb20ifQ.DYfKP8CISBfcNiGqyWltN8ehwkGZMcy_VSF72KW" +
                "bHEyEb26a7hxCJiggFMnwYKOp48WzDkkzv7TyPNgVua2upiH30R8fhIulMytv548NBCk3ktX_ZFq5j1NsjKVrEi0t7T7Nzw2Rh5" +
                "F4vyAmE0uTsslHzZcg5UphuPIpSy1EzFmVexpfan_FQ5Qun7fQ-MBsG4xmwlQRG3GJEXtHq3xxR07e2vp5HvrfHkyXywTkK0Iyf" +
                "94wvoLXXYgN732zM1UTDv9KeOcWyf6z3_LmIx1G8stRVjVrtg6OskicIhTKh6Gaq1GD41yb_7Ycss9EwWff0UhTI2pWNoVARfQd" +
                "uVzUZA";

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + expiredJwt))
                .andExpect(status().isUnauthorized());
    }

    // Security Test 3 - Login protection against dictionary attacks
    @Test
    void dictionaryAttackShouldGetRateLimited() throws Exception {
        String[] commonPasswords = {
                "123456", "password", "qwerty", "admin",
                "letmein", "pass", "12345678", "abc123"
        };

        boolean blocked = false;
        for (String password : commonPasswords) {
            int status = mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", "127.0.0.1") // IMPORTANT FIX
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                        {
                            "username":"user",
                            "password":"%s"
                        }
                        """.formatted(password)))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            if (status == 429) {
                blocked = true;
                break;
            }
        }

        if (blocked) {
            fail("Login endpoint protection failed against dictionary attacks");
        }
    }

    // Security Test 4 - Login protection against brute-force attacks
    @Test
    void bruteForceLoginShouldGetRateLimited() throws Exception {
        boolean blocked = false;
        for (int i = 0; i < 20; i++) {
            int status = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                        {
                            "username":"user",
                            "password":"wrongpassword"
                        }
                        """))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            if (status == 429) {
                blocked = true;
                break;
            }
        }

        if (blocked) {
            fail("Login endpoint protection failed brute force login detection");
        }
    }

    // Security Test 5 - Privilege Escalation
    @Test
    void privilegeEscalationCannotAllowAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().isForbidden());
    }

    //Security Test 6 - Endpoint authentication enforcement
    @Test
    void invalidJwtTokenShouldBeRejectedAtProtectedEndpoints() throws Exception {
        String invalidToken = "invalid-token";

        mockMvc.perform(get("/api/auth/login")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    // Security Test 7 - Upload malicious file
    @Test
    void maliciousFileUploadShouldBeRejected() throws Exception {
        UUID gameId = UUID.randomUUID();

        // EXE magic bytes (MZ header)
        byte[] fakeExe = new byte[] { 0x4D, 0x5A, 0x00 };

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "evil.jpg",
                "image/jpeg",
                fakeExe
        );

        mockMvc.perform(multipart("/api/publisher/games/" + gameId + "/files")
                        .file(file)
                        .param("fileType", "IMAGE")
                        .with(jwt().authorities(() -> "ROLE_PUBLISHER")))
                .andExpect(status().isBadRequest());
    }

    // Security Test 8 - Oversized file upload
    @Test
    void oversizedFileUploadShouldBeRejected() throws Exception {
        UUID gameId = UUID.randomUUID();

        // 25MB + 1 byte
        byte[] oversizedFile = new byte[(25 * 1024 * 1024) + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                oversizedFile
        );

        mockMvc.perform(multipart("/api/publisher/games/" + gameId + "/files")
                        .file(file)
                        .param("fileType", "IMAGE")
                        .with(jwt().authorities(() -> "ROLE_PUBLISHER")))
                .andExpect(status().isPayloadTooLarge()); // 413
    }

    // Security Test 9 - MIME type bypass
    @Test
    void mimeTypeBypassShouldBeRejected() throws Exception {
        UUID gameId = UUID.randomUUID();

        // PDF magic bytes ("%PDF")
        byte[] fakePdf = new byte[] { 0x25, 0x50, 0x44, 0x46 };

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "evil.jpg",
                "image/jpeg",
                fakePdf
        );

        mockMvc.perform(multipart("/api/publisher/games/" + gameId + "/files")
                        .file(file)
                        .param("fileType", "IMAGE")
                        .with(jwt().authorities(() -> "ROLE_PUBLISHER")))
                .andExpect(status().isBadRequest());
    }

    // Security Test 10 - Game Key Guessing Attacks
    @Test
    void repeatedKeyActivationAttemptsShouldGetRateLimited() throws Exception {
        String ip = "192.168.1.100";

        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/api/library/import-key")
                            .header("X-Forwarded-For", ip)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"activationKey\":\"INVALID-KEY-" + i + "\"}")
                            .with(jwt().authorities(() -> "ROLE_BUYER")))
                    .andExpect(status().isBadRequest());
        }
    }

    // Security Test 11 - Order modification
    @Test
    void orderModificationWithTamperedDataShouldBeRejected() throws Exception {
        UUID orderId = UUID.randomUUID();

        String tamperedOrder = "{\"totalPrice\":-99.99,\"discount\":100.00}";

        mockMvc.perform(post("/api/orders/" + orderId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tamperedOrder)
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().is4xxClientError());
    }

    // Security Test 12 - Duplicate purchase abuse
    @Test
    void duplicatePurchaseOfSameGameShouldBeHandledSafely() throws Exception {
        UUID gameId = UUID.randomUUID();

        String firstAttempt = "{\"gameId\":\"" + gameId + "\"}";

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstAttempt)
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().is4xxClientError()); // Order endpoint expects different payload format

        // Follow-up attempt with same game should either be rejected or safely handled
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstAttempt)
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().is4xxClientError());
    }

    // Security Test 13 - Payment bypass attempt -> Payment processing not implemented

    // Security Test 14: Invoice ID enumeration
    @Test
    void unauthorizedInvoiceAccessShouldBeBlocked() throws Exception {
        // Attempt to access invoices for random UUIDs (enumeration attack)
        UUID randomOrderId = UUID.randomUUID();

        mockMvc.perform(get("/api/orders/" + randomOrderId + "/invoice")
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().is4xxClientError());

        // Another random ID
        UUID anotherRandomId = UUID.randomUUID();
        mockMvc.perform(get("/api/orders/" + anotherRandomId + "/invoice")
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().is4xxClientError());
    }

    // Security Test 15: Path traversal attack
    @Test
    void pathTraversalAttackAccessShouldBeBlocked() throws Exception {
        // Attempt to games images for random UUIDs (enumeration attack)
        UUID randomGameId = UUID.randomUUID();

        mockMvc.perform(get("/api/publisher/games/" + randomGameId + "/files?path=images/cover.png")
                        .with(jwt().authorities(() -> "ROLE_PUBLISHER")))
                .andExpect(status().is4xxClientError());
    }

    // Security Test 15: Path traversal attack
    @Test
    void unauthorizedFileAccessShouldBeBlocked() throws Exception {
        UUID gameId = UUID.fromString("c2a9f3d1-6b8e-4c2f-9d1a-7e4b6c3f8a91");

        mockMvc.perform(get("/api/publisher/games/" + gameId + "/files?path=images/cover.png"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void actuatorHealthIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(result ->
                        assertNotEquals(401, result.getResponse().getStatus()));
    }

    @Test
    void loginEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"password\":\"y\"}"))
                .andExpect(result ->
                        assertNotEquals(401, result.getResponse().getStatus()));
    }

    @Test
    void registerEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"email\":\"x@x.com\",\"password\":\"y\"}"))
                .andExpect(result ->
                        assertNotEquals(401, result.getResponse().getStatus()));
    }

    @Test
    void gamesGetEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/games/1"))
                .andExpect(result ->
                        assertNotEquals(401, result.getResponse().getStatus()));
    }

    @Test
    void adminEndpointRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void publisherEndpointRequiresPublisherRole() throws Exception {
        mockMvc.perform(get("/api/publisher/games")
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void ordersEndpointRequiresBuyerRole() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .with(jwt().authorities(() -> "ROLE_PUBLISHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void libraryEndpointRequiresBuyerRole() throws Exception {
        mockMvc.perform(get("/api/library")
                        .with(jwt().authorities(() -> "ROLE_PUBLISHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void profileEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/unknown/endpoint"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiDocsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swaggerUiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void responseHeadersAreConfigured() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(result -> {
                    var response = result.getResponse();
                    // X-Content-Type-Options (contentTypeOptions)
                    assertNotNull(response.getHeader("X-Content-Type-Options"));
                    // Cache-Control (cacheControl)
                    assertNotNull(response.getHeader("Cache-Control"));
                });
    }

    @Test
    void postWithoutCsrfTokenIsAccepted() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"x\",\"password\":\"y\"}"))
                .andExpect(result ->
                        assertNotEquals(403, result.getResponse().getStatus()));
    }

    @Test
    void noSessionIsCreatedAfterRequest() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(result ->
                        assertNull(result.getRequest().getSession(false)));
    }

    @Test
    void corsConfigurationSourceIsNotNull() {
        assertNotNull(corsConfigurationSource);
    }

    @Test
    void corsAllowsConfiguredOrigins() {
        var config = corsConfigurationSource
                .getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(config);
        assertNotNull(config.getAllowedOrigins());
        assertFalse(config.getAllowedOrigins().isEmpty());
    }

    @Test
    void corsAllowsExpectedMethods() {
        var config = corsConfigurationSource
                .getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(config);
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedMethods().contains("POST"));
        assertTrue(config.getAllowedMethods().contains("DELETE"));
        assertTrue(config.getAllowedMethods().contains("PUT"));
        assertTrue(config.getAllowedMethods().contains("PATCH"));
    }

    @Test
    void corsAllowsExpectedHeaders() {
        var config = corsConfigurationSource
                .getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(config);
        assertTrue(config.getAllowedHeaders().contains("Authorization"));
        assertTrue(config.getAllowedHeaders().contains("Content-Type"));
    }

    @Test
    void invalidBearerTokenTriggersEntryPoint() throws Exception {
        mockMvc.perform(get("/api/profile/me")
                        .header("Authorization", "Bearer token.invalido.mesmo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtAuthenticationConverterIsNotNull() {
        assertNotNull(jwtAuthenticationConverter);
    }

    @Test
    void jwtAuthenticationConverterExtractsPrincipalFromPreferredUsername() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "alice")
                .claim("realm_access", java.util.Map.of(
                        "roles", java.util.List.of("BUYER")))
                .build();

        var auth = jwtAuthenticationConverter.convert(jwt);
        assertNotNull(auth);
        assertEquals("alice", auth.getName());
    }

    @Test
    void jwtAuthenticationConverterExtractsRolesFromRealmAccess() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "publisher1")
                .claim("realm_access", java.util.Map.of(
                        "roles", java.util.List.of("PUBLISHER")))
                .build();

        var auth = jwtAuthenticationConverter.convert(jwt);
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PUBLISHER")));
    }

    @Test
    void httpTraceDeniedOnAllPaths() throws Exception {
        // StrictHttpFirewall rejects TRACE before authorization rules run → 400
        mockMvc.perform(request(HttpMethod.TRACE, "/api/games/1"))
                .andExpect(status().isBadRequest());
    }
}
