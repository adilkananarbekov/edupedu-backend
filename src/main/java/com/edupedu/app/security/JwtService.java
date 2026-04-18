package com.edupedu.app.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

@Service
public class JwtService {

    public static final String TOKEN_TYPE = "token_type";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH_TOKEN";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    @Value("${app.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public JwtService(
            @Value("${app.security.jwt.private-key-path}") final String privateKeyPath,
            @Value("${app.security.jwt.public-key-path}") final String publicKeyPath
    ) throws Exception {
        this.privateKey = KeyUtils.loadPrivateKey(privateKeyPath);
        this.publicKey = KeyUtils.loadPublicKey(publicKeyPath);
    }

    public String generateAccessToken(final String username) {
        final Map<String, Object> claims = Map.of(TOKEN_TYPE, "ACCESS_TOKEN");
        return buildToken(username, claims, this.accessTokenExpiration);
    }

    public String generateRefreshToken(final String username) {
        final Map<String, Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN");
        return buildToken(username, claims, this.refreshTokenExpiration);
    }

    public String buildToken(final String username, final Map<String, Object> claims, final long expiration) {
        return Jwts.builder()
                   .id(UUID.randomUUID().toString())
                   .claims(claims)
                   .subject(username)
                   .issuedAt(new Date(System.currentTimeMillis()))
                   .expiration(new Date(System.currentTimeMillis() + expiration))
                   .signWith(this.privateKey)
                   .compact();
    }

    public boolean isTokenValid(final String token, final String expectedUsername) {
        final String username = extractUsername(token);
        return username.equals(expectedUsername) && !isTokenExpired(token);
    }

    public String extractUsername(final String token) {
        return extractClaims(token).getSubject();
    }

    private boolean isTokenExpired(final String token) {
        return extractClaims(token).getExpiration()
                                   .before(new Date());
    }
    private boolean isExpectedTokenType(final Claims claims, final String expectedTokenType) {
        return expectedTokenType.equals(claims.get(TOKEN_TYPE, String.class));
    }

    private Claims extractClaims(final String token) {
        try {
            return Jwts.parser()
                       .verifyWith(this.publicKey)
                       .build()
                       .parseSignedClaims(token)
                       .getPayload();
        } catch (final JwtException e) {
            throw new RuntimeException("Invalid token", e);
        }
    }
    public String extractTokenId(final String token) {
        return extractClaims(token).getId();
    }
    public Date extractExpiration(final String token) {
        return extractClaims(token).getExpiration();
    }

    public boolean validateToken(final String token, final String expectedUsername, final String expectedTokenType) {
        final Claims claims = extractClaims(token);
        final String username = claims.getSubject();
        return StringUtils.hasText(username)
                && username.equals(expectedUsername)
                && !isTokenExpired(token)
                && isExpectedTokenType(claims, expectedTokenType);
    }

    public String refreshAccessToken(final String refreshToken) {
        final Claims claims = extractClaims(refreshToken);

        if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE, String.class))) {
            throw new RuntimeException("Invalid token type");
        }
        if (claims.getExpiration().before(new Date())) {
            throw new RuntimeException("Refresh token expired");
        }

        final String username = claims.getSubject();
        return generateAccessToken(username);
    }
}
