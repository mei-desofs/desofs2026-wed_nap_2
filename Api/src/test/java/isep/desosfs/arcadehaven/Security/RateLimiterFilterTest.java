package isep.desosfs.arcadehaven.Security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimiterFilterTest {
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(mock(SecurityAuditService.class));
    }

    @Test
    void testNonRateLimitedPath_passesThrough() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getRequestURI()).thenReturn("/api/other/path");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testRateLimitedPath_underLimit_passesThrough() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("1.2.3.4");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void testRateLimitedPath_exceedLimit_returns429() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        when(request.getRequestURI()).thenReturn("/api/auth/register");
        when(request.getRemoteAddr()).thenReturn("2.3.4.5");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        for (int i = 0; i < 30; i++) {
            filter.doFilter(request, mock(HttpServletResponse.class), chain);
        }
        filter.doFilter(request, response, chain);

        verify(response).setStatus(429);
        verify(response).setContentType("application/json");

        String json = sw.toString();
        assertTrue(json.contains("Too many requests"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testResolveClientIp_withXForwardedFor() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1,10.0.0.2");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        filter.doFilter(request, response, chain);
        for (int i = 0; i < 29; i++) {
            filter.doFilter(request, mock(HttpServletResponse.class), chain);
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);
        filter.doFilter(request, response, chain);

        verify(response).setStatus(429);
        String json = sw.toString();
        assertTrue(json.contains("Too many requests"));
    }
}
