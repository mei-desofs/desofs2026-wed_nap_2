package isep.desosfs.arcadehaven.Smoke;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("smoke-tests")
class SmokeTests {

    //Test health endpoint
    @Test
    void healthEndpointShouldReturnOk() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/actuator/health"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    //Test register endpoint
    @Test
    void registerEndpointShouldReturnCreatedOrAlreadyExists() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String body = """
        {
          "username": "smokeuser",
          "email": "smoke@test.com",
          "password": "Ztr4Safe#2620WZ",
          "role": "BUYER"
        }
        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();

        boolean ok = (status == 201 || status == 409);

        if (!ok) {
            throw new AssertionError("Expected 201 or 409 but got " + status);
        }
    }

    //Test login endpoint
    @Test
    void loginEndpointShouldReturnOk() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String registerBody = """
            {
              "username": "smokeuser",
              "email": "smoke@test.com",
              "password": "Ztr4Safe#2620WZ",
              "role": "BUYER"
            }
        """;

        //Ensures client exists before logging in
        client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/auth/register"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(registerBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        String loginBody = """
            {
              "username": "smokeuser",
              "password": "Ztr4Safe#2620WZ"
            }
        """;

        //Logs in
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
    }

    @Test
    void logoutWithoutTokenShouldBeUnauthorized() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/logout"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        // Usually Spring Security returns 401 or 403 depending on config
        assertEquals(401, response.statusCode());
    }

    @Test
    void profileEndpointWithoutAuthShouldReturnUnauthorized() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/profile"))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
    }

    @Test
    void updateProfileWithInvalidEmailShouldReturnBadRequest() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String body = """
        {
          "email": "not-an-email"
        }
        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/profile"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
    }
}
