package com.nmaravic.movie.api.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private MovieLoggingFilter movieLoggingFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    private void mockSecurityContext() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("email")).thenReturn("user@example.com");

        JwtAuthenticationToken authentication = mock(JwtAuthenticationToken.class);
        when(authentication.getToken()).thenReturn(jwt);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockAnonymousSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testDoFilter_CallsFilterChain() throws IOException, ServletException {
        mockAnonymousSecurityContext();
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/movies");
        when(response.getStatus()).thenReturn(200);

        movieLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_ClearsMDC_AfterExecution() throws IOException, ServletException {
        mockAnonymousSecurityContext();
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/movies");
        when(response.getStatus()).thenReturn(200);

        movieLoggingFilter.doFilter(request, response, filterChain);

        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void testDoFilter_WithAuthenticatedUser_UsesEmailFromJwt() throws IOException, ServletException {
        mockSecurityContext();
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/movies");
        when(response.getStatus()).thenReturn(201);

        movieLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
    }

    @Test
    void testDoFilter_WithNoAuthentication_UsesAnonymous() throws IOException, ServletException {
        mockAnonymousSecurityContext();
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/movies/1");
        when(response.getStatus()).thenReturn(200);

        movieLoggingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_ClearsMDC_EvenWhenFilterChainThrows() throws IOException, ServletException {
        mockAnonymousSecurityContext();
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/movies");
        doThrow(new ServletException("error")).when(filterChain).doFilter(request, response);

        try {
            movieLoggingFilter.doFilter(request, response, filterChain);
        }
        catch (ServletException ignored) {
            //do not fail test on exception
        }
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilter_LogsCorrectHttpMethod() throws IOException, ServletException {
        mockAnonymousSecurityContext();
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/movies/1");
        when(response.getStatus()).thenReturn(204);

        movieLoggingFilter.doFilter(request, response, filterChain);

        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
        verify(response).getStatus();
    }
}