package pl.jit.flashcards.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;

@Component
@Slf4j
public class JwtTokenProvider {

    private final Environment environment;
    @Getter
    private long jwtExpirationAccessMs;
    private long jwtExpirationRefreshMs;
    private SecretKey signingKey;

    public JwtTokenProvider(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    private void initialize() {
        String jwtSecret = Objects.requireNonNull(environment.getProperty("jwt.secret"), "jwt.secret must be set");
        this.jwtExpirationAccessMs = Long.parseLong(Objects.requireNonNull(environment.getProperty("jwt.expiration.access"), "jwt.expiration.access must be set"));
        this.jwtExpirationRefreshMs = Long.parseLong(Objects.requireNonNull(environment.getProperty("jwt.expiration.refresh"), "jwt.expiration.refresh must be set"));

        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT configuration loaded. Access token expiration: {}ms, Refresh token expiration: {}ms", jwtExpirationAccessMs, jwtExpirationRefreshMs);
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    public String generateAccessToken(Authentication authentication) {
        String userId = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationAccessMs);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        String userId = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationRefreshMs);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }


    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }
}
