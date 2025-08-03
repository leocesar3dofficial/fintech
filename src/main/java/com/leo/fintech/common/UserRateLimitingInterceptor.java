package com.leo.fintech.common;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.leo.fintech.auth.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserRateLimitingInterceptor implements HandlerInterceptor {

    private static class RateLimitInfo {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private volatile LocalDateTime windowStart = LocalDateTime.now();
        private volatile LocalDateTime lastAccessed = LocalDateTime.now();
        private static final int MAX_REQUESTS = 30;
        private static final long WINDOW_MINUTES = 1;

        public boolean tryConsume() {
            LocalDateTime now = LocalDateTime.now();
            lastAccessed = now;

            if (ChronoUnit.MINUTES.between(windowStart, now) >= WINDOW_MINUTES) {
                synchronized (this) {
                    if (ChronoUnit.MINUTES.between(windowStart, now) >= WINDOW_MINUTES) {
                        windowStart = now;
                        requestCount.set(0);
                    }
                }
            }

            return requestCount.incrementAndGet() <= MAX_REQUESTS;
        }

        public boolean isStale(LocalDateTime now, long maxIdleMinutes) {
            return ChronoUnit.MINUTES.between(lastAccessed, now) >= maxIdleMinutes;
        }
    }

    private final Map<UUID, RateLimitInfo> rateLimits = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupTime = new AtomicLong(System.currentTimeMillis());

    private static final long CLEANUP_INTERVAL_MILLIS = 60_000; // 1 minute
    private static final long MAX_IDLE_MINUTES = 5; // Evict entries idle for 5+ minutes

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) throws IOException {
        UUID userId;
        
        try {
            userId = SecurityUtils.extractUserId();
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Unauthorized");
            return false;
        }

        cleanupStaleEntriesIfNeeded();

        RateLimitInfo rateLimitInfo = rateLimits.computeIfAbsent(userId, _ -> new RateLimitInfo());

        if (rateLimitInfo.tryConsume()) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests - rate limit exceeded");
            return false;
        }
    }

    private void cleanupStaleEntriesIfNeeded() {
        long nowMillis = System.currentTimeMillis();

        if (nowMillis - lastCleanupTime.get() > CLEANUP_INTERVAL_MILLIS) {
            if (lastCleanupTime.compareAndSet(lastCleanupTime.get(), nowMillis)) {
                LocalDateTime now = LocalDateTime.now();
                rateLimits.entrySet().removeIf(entry -> entry.getValue().isStale(now, MAX_IDLE_MINUTES));
            }
        }
    }
}
