package com.leo.fintech.logging;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GlobalLoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req) {
            String method = req.getMethod();
            String fullPath = req.getRequestURI();
            String queryString = req.getQueryString();

            if (queryString != null) {
                fullPath += "?" + queryString;
            }

            log.info("[GLOBAL LOG] Incoming request: {} {}", method, fullPath);
        }

        chain.doFilter(request, response);
    }
}
