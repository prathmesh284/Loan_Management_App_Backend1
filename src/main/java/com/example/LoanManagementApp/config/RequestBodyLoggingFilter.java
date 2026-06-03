package com.example.LoanManagementApp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RequestBodyLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestBodyLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only wrap and log the signup endpoint to avoid sensitive data in logs broadly
        if ("/api/auth/signup".equalsIgnoreCase(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
            ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);
            try {
                filterChain.doFilter(wrapper, response);
            } finally {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    String payload = new String(buf, StandardCharsets.UTF_8);
                    log.info("Incoming signup request body: {}", payload);
                } else {
                    log.info("Incoming signup request body: <empty>");
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
