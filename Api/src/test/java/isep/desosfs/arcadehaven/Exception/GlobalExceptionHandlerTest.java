package isep.desosfs.arcadehaven.Exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import isep.desosfs.arcadehaven.Security.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;

public class GlobalExceptionHandlerTest {
    GlobalExceptionHandler handler;
    SecurityAuditService auditService;

    @BeforeEach
    void setup() {
        auditService = mock(SecurityAuditService.class);
        handler = new GlobalExceptionHandler(auditService);
    }

    @Test
    void shouldHandleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<Map<String, Object>> response =
                handler.handleMethodNotSupported(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(405, response.getBody().get("status"));
    }

    @Test
    void shouldHandleResourceNotFound() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("User not found");

        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().get("message"));
    }

    @Test
    void shouldHandleBusinessException() {
        BusinessException ex =
                new BusinessException("Business error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleBusiness(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Business error", response.getBody().get("message"));
    }

    @Test
    void shouldHandleIllegalArgument() {
        IllegalArgumentException ex =
                new IllegalArgumentException("Invalid argument");

        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldHandleIllegalState() {
        IllegalStateException ex =
                new IllegalStateException("Invalid state");

        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void shouldHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Map<String, Object>> response =
                handler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().get("message"));
    }

    @Test
    void shouldHandleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Denied");
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Map<String, Object>> response =
                handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().get("message"));
    }

    @Test
    void shouldHandleValidationErrors() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");

        bindingResult.addError(
                new FieldError(
                        "object",
                        "username",
                        "Username is required"
                )
        );

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> errors =
                (Map<String, String>) response.getBody().get("errors");

        assertEquals("Username is required", errors.get("username"));
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new Exception("Unexpected");

        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }
}
