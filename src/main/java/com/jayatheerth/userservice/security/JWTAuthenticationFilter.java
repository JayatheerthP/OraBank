package com.jayatheerth.userservice.security;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

/**
 * Custom filter for JWT-based authentication.
 * This filter intercepts HTTP requests to validate JWT tokens in the
 * Authorization header,
 * extracts the user ID, and sets the authentication context for secured
 * endpoints.
 */
@Component
@AllArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger filterlogger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
    private final JWTService jwtService;

    

    /**
     * Filters incoming HTTP requests to check for a valid JWT token in the
     * Authorization header.
     * If a valid token is found, extracts the user ID and sets the authentication
     * context.
     *
     * @param request     The incoming HTTP request.
     * @param response    The HTTP response.
     * @param filterChain The filter chain to continue processing the request.
     * @throws ServletException If a servlet-related error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            UUID userId = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // Extract token after "Bearer "
                filterlogger.debug("JWT token found in Authorization header");
                if (jwtService.isTokenValid(token)) {
                    userId = jwtService.extractUserId(token);
                    filterlogger.info("Valid JWT token processed for userId: {}", userId);
                } else {
                    filterlogger.warn("Invalid or expired JWT token provided");
                }
            } else {
                filterlogger.debug("No Bearer token found in Authorization header for request: {}",
                        request.getRequestURI());
            }

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Create authentication object and set it in the security context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId, token, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterlogger.debug("Authentication set in security context for userId: {}", userId);
            }

            filterChain.doFilter(request, response);
        } catch (ServletException | IOException e) {
            filterlogger.error("Error processing JWT authentication filter: {}", e.getMessage(), e);
            filterChain.doFilter(request, response); // Continue the chain even if an error occurs to avoid blocking
        }
    }
}