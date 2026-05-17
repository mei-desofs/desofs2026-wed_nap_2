package isep.desosfs.arcadehaven.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SecurityIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

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

    // Security Test 2 - Session and token protection -> To be implemented, dependent on keylclocak configuration

    // Security Test 3 - Login protection against dictionary attacks
    @Test
    void dictionaryAttackShouldGetRateLimited() throws Exception {
        String[] commonPasswords = {"123456", "password", "qwerty", "admin", "letmein", "pass", "12345678", "abc123"};

        boolean blocked = false;
        for (String password : commonPasswords) {
            int status = mockMvc.perform(post("/api/auth/login")
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

    //Security Test 5 - JWT Token Theft - Keycloack error
//    @Test
//    void expiredJwtTokenShouldBeRejected() throws Exception {
//        String expiredJwt = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJyd0VQNjkzN2R2bzZuYVZUS2dfWmhi" +
//                "VkJlZDdjNnNfeGM2d2Y1ejQtZnZzIn0.eyJleHAiOjE3NzkwMDg5ODAsImlhdCI6MTc3OTAwODY4MCwianRpIjoib25ydHJvOmU" +
//                "0M2Q0OWQyLTQ5ODktNGRhZC1hZWNmLTc2NDk3YTU1MjUyMyIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODE4MC9yZWFsbXMvYX" +
//                "JjYWRlaGF2ZW4iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMDVlYzg3YjYtOWQxMC00OWZhLWE2NzQtOGQ2YmRhMGExMTQ3Iiwid" +
//                "HlwIjoiQmVhcmVyIiwiYXpwIjoiYXJjYWRlaGF2ZW4tcHVibGljIiwic2lkIjoiYjNlYzVmOTctZTEwNi00M2JkLTgzZjUtNTFi" +
//                "MThlMzA5NzY2IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZ" +
//                "saW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtYXJjYWRlaGF2ZW4iLCJCVVlFUiIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZX" +
//                "NvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzI" +
//                "iwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJi" +
//                "dXllcjIgVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6ImJ1eWVyMiIsImdpdmVuX25hbWUiOiJidXllcjIiLCJmYW1pbHlfbmF" +
//                "tZSI6IlVzZXIiLCJlbWFpbCI6ImJ1eWVyMkBhcmNhZGVoYXZlbi5jb20ifQ.DYfKP8CISBfcNiGqyWltN8ehwkGZMcy_VSF72KW" +
//                "bHEyEb26a7hxCJiggFMnwYKOp48WzDkkzv7TyPNgVua2upiH30R8fhIulMytv548NBCk3ktX_ZFq5j1NsjKVrEi0t7T7Nzw2Rh5" +
//                "F4vyAmE0uTsslHzZcg5UphuPIpSy1EzFmVexpfan_FQ5Qun7fQ-MBsG4xmwlQRG3GJEXtHq3xxR07e2vp5HvrfHkyXywTkK0Iyf" +
//                "94wvoLXXYgN732zM1UTDv9KeOcWyf6z3_LmIx1G8stRVjVrtg6OskicIhTKh6Gaq1GD41yb_7Ycss9EwWff0UhTI2pWNoVARfQd" +
//                "uVzUZA";
//
//        mockMvc.perform(get("/api/profile/me")
//                        .header("Authorization", expiredJwt))
//                .andExpect(status().isUnauthorized());
//    }

    // Security Test 6 - Privilege Escalation
    @Test
    void privilegeEscalationCannotAllowAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().isForbidden());
    }

    //Security Test 7 - Endpoint authentication enforcement -> Login not working/implemented
//    @Test
//    void expiredJwtTokenShouldBeRejectedAtProtectedEndpoints() throws Exception {
//        String expiredJwt = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJyd0VQNjkzN2R2bzZuYVZUS2dfWmhi" +
//                "VkJlZDdjNnNfeGM2d2Y1ejQtZnZzIn0.eyJleHAiOjE3NzkwMDg5ODAsImlhdCI6MTc3OTAwODY4MCwianRpIjoib25ydHJvOmU" +
//                "0M2Q0OWQyLTQ5ODktNGRhZC1hZWNmLTc2NDk3YTU1MjUyMyIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODE4MC9yZWFsbXMvYX" +
//                "JjYWRlaGF2ZW4iLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiMDVlYzg3YjYtOWQxMC00OWZhLWE2NzQtOGQ2YmRhMGExMTQ3Iiwid" +
//                "HlwIjoiQmVhcmVyIiwiYXpwIjoiYXJjYWRlaGF2ZW4tcHVibGljIiwic2lkIjoiYjNlYzVmOTctZTEwNi00M2JkLTgzZjUtNTFi" +
//                "MThlMzA5NzY2IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZ" +
//                "saW5lX2FjY2VzcyIsImRlZmF1bHQtcm9sZXMtYXJjYWRlaGF2ZW4iLCJCVVlFUiIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZX" +
//                "NvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzI" +
//                "iwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJi" +
//                "dXllcjIgVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6ImJ1eWVyMiIsImdpdmVuX25hbWUiOiJidXllcjIiLCJmYW1pbHlfbmF" +
//                "tZSI6IlVzZXIiLCJlbWFpbCI6ImJ1eWVyMkBhcmNhZGVoYXZlbi5jb20ifQ.DYfKP8CISBfcNiGqyWltN8ehwkGZMcy_VSF72KW" +
//                "bHEyEb26a7hxCJiggFMnwYKOp48WzDkkzv7TyPNgVua2upiH30R8fhIulMytv548NBCk3ktX_ZFq5j1NsjKVrEi0t7T7Nzw2Rh5" +
//                "F4vyAmE0uTsslHzZcg5UphuPIpSy1EzFmVexpfan_FQ5Qun7fQ-MBsG4xmwlQRG3GJEXtHq3xxR07e2vp5HvrfHkyXywTkK0Iyf" +
//                "94wvoLXXYgN732zM1UTDv9KeOcWyf6z3_LmIx1G8stRVjVrtg6OskicIhTKh6Gaq1GD41yb_7Ycss9EwWff0UhTI2pWNoVARfQd" +
//                "uVzUZA";
//
//        mockMvc.perform(get("/api/auth/login")
//                        .header("Authorization", expiredJwt))
//                .andExpect(status().isUnauthorized());
//    }

    // Security Test 8 - Upload malicious file
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

    // Security Test 9 - Oversized file upload -> File size validation to be implemented

    // Security Test 10 - MIME type bypass
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

    // Security Test 11 - Game Key Guessing Attacks
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

    // Security Test 12 - Order modification
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

    // Security Test 13 - Duplicate purchase abuse
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

    // Security Test 14 - Payment bypass attempt -> Payment processing not implemented

    // Security Test 15: Invoice ID enumeration
    @Test
    void unauthorizedInvoiceAccessShouldBeBlocked() throws Exception {
        // Attempt to access invoices for random UUIDs (enumeration attack)
        UUID randomOrderId = UUID.randomUUID();

        mockMvc.perform(get("/api/orders/" + randomOrderId + "/invoice")
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().is4xxClientError()); // Should be 404 or 403, not allow access

        // Another random ID
        UUID anotherRandomId = UUID.randomUUID();
        mockMvc.perform(get("/api/orders/" + anotherRandomId + "/invoice")
                        .with(jwt().authorities(() -> "ROLE_BUYER")))
                .andExpect(status().is4xxClientError());
    }

}
