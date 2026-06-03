package com.nmaravic.movie.api.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    @Value("${rate.limit.authenticated}")
    private int authenticatedCapacity;

    @Value("${rate.limit.anonymous}")
    private int anonymousCapacity;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        String key = resolveKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(isAuthenticated()));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        long retryAfter = probe.getNanosToWaitForRefill() / 1_000_000_000;
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfter));
        response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Rate limit exceeded. Retry after " + retryAfter + "s.");
        return false;
    }

    private Bucket createBucket(boolean authenticated) {
        int capacity = authenticated ? authenticatedCapacity : anonymousCapacity;
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private String resolveKey(HttpServletRequest request) {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken().getClaimAsString("preferred_username"))
                .orElseGet(request::getRemoteAddr);
    }

    private boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken;
    }
}