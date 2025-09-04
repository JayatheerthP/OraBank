package com.jayatheerth.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.jayatheerth.userservice.exception.UserServiceException;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;

/**
 * Service class for handling JSON Web Token (JWT) operations.
 * This class provides methods for generating, validating, and extracting
 * information from JWTs
 * used for user authentication in the application.
 */
@Service
public class JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    /**
     * Retrieves the signing key for JWT operations.
     * The key is derived from the configured secret key.
     *
     * @return Key The signing key for JWT signing and verification.
     */
    private Key getSigningKey() {
        try {
            return Keys.hmacShaKeyFor(secretKey.getBytes());
        } catch (WeakKeyException e) {
            logger.error("Error generating signing key for JWT: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to generate JWT signing key", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Generates a JWT token for the specified user ID.
     * The token includes the user ID as a claim and is signed with the configured
     * key.
     *
     * @param userId The UUID of the user for whom the token is generated.
     * @param email The email of the user.
     * @param firstName The first name.
     * @return String The generated JWT token.
     */
    public String generateToken(UUID userId, String email, String firstName) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId.toString());
            claims.put("email", email);
            claims.put("firstName", firstName);

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId.toString())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 1000))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();

            logger.info("JWT token generated successfully for userId: {}", userId);
            return token;
        } catch (InvalidKeyException e) {
            logger.error("Error generating JWT token for userId: {}. Error: {}", userId, e.getMessage(), e);
            throw new UserServiceException("Failed to generate JWT token for userId: " + userId, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extracts the user ID from the provided JWT token.
     *
     * @param token The JWT token from which to extract the user ID.
     * @return UUID The user ID extracted from the token.
     */
    public UUID extractUserId(String token) {
        try {
            String userIdStr = extractClaim(token, Claims::getSubject);
            return UUID.fromString(userIdStr);
        } catch (Exception e) {
            logger.error("Error extracting userId from JWT token: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to extract userId from JWT token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extracts a specific claim from the provided JWT token using a resolver
     * function.
     *
     * @param <T>            The type of the claim to extract.
     * @param token          The JWT token from which to extract the claim.
     * @param claimsResolver The function to resolve the desired claim from the
     *                       Claims object.
     * @return T The extracted claim value.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            logger.error("Error extracting claim from JWT token: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to extract claim from JWT token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extracts all claims from the provided JWT token.
     *
     * @param token The JWT token from which to extract claims.
     * @return Claims The claims contained in the token.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | SignatureException | IllegalArgumentException e) {
            logger.error("Error extracting all claims from JWT token: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to extract claim from JWT token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validates the provided JWT token.
     * Checks if the token is well-formed and not expired.
     *
     * @param token The JWT token to validate.
     * @return boolean True if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token) {
        try {
            boolean isValid = !isTokenExpired(token);
            logger.debug("Token validation result for token: {}. Valid: {}", token.substring(0, 10) + "...", isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating JWT token: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if the provided JWT token is expired.
     *
     * @param token The JWT token to check.
     * @return boolean True if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.error("Error checking if JWT token is expired: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to check if JWT token is expired", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Extracts the expiration date from the provided JWT token.
     *
     * @param token The JWT token from which to extract the expiration date.
     * @return Date The expiration date of the token.
     */
    private Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
            logger.error("Error extracting expiration date from JWT token: {}", e.getMessage(), e);
            throw new UserServiceException("Failed to extract expiration date from JWT token", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    /**
     * Retrieves the configured expiration time for JWT tokens.
     *
     * @return long The expiration time in seconds.
     */
    public long getExpirationTime() {
        return expirationTime;
    }
}