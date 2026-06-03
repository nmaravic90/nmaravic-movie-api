package com.nmaravic.movie.api.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class MovieLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(MovieLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String traceId = UUID.randomUUID().toString();
        String userEmail = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken().getClaimAsString("email"))
                .orElse("anonymous");

        MDC.put("traceId", traceId);
        long startTime = System.currentTimeMillis();
        log.info("[{}] [{}] → {} {}", traceId, userEmail, httpRequest.getMethod(), httpRequest.getRequestURI());
        chain.doFilter(request, response);
        long duration = System.currentTimeMillis() - startTime;
        log.info("[{}] [{}] ← {} {} | status={} | {}ms", traceId, userEmail, httpRequest.getMethod(), httpRequest.getRequestURI(), httpResponse.getStatus(), duration);
        MDC.clear();
    }
}
