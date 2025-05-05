package pl.jit.flashcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private static final String DUMMY_JWT_SECRET_BASE64 = Base64.getEncoder().encodeToString("DummySecretForConstructorWhichIsAlsoLongEnoughForHS512AlgorithmMaybe".getBytes());
    private static final long TEST_ACCESS_EXPIRATION_MS = 60000;
    private static final long TEST_REFRESH_EXPIRATION_MS = 300000;

    @Mock
    private Environment environment;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetailsImpl userDetails;

    private JwtTokenProvider jwtTokenProvider;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        // given
        when(environment.getProperty("jwt.secret")).thenReturn(DUMMY_JWT_SECRET_BASE64);
        when(environment.getProperty("jwt.expiration.access")).thenReturn(String.valueOf(TEST_ACCESS_EXPIRATION_MS));
        when(environment.getProperty("jwt.expiration.refresh")).thenReturn(String.valueOf(TEST_REFRESH_EXPIRATION_MS));

        // when
        jwtTokenProvider = new JwtTokenProvider(environment);

        // then - przygotowanie wspólnych danych
        testUserId = UUID.randomUUID();
    }

    private void setupAuthenticationMocks() {
        when(userDetails.getId()).thenReturn(testUserId);
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void generateAccessToken_shouldGenerateValidToken_whenAuthenticationIsValid() {
        // given
        setupAuthenticationMocks();

        // when
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        // then
        assertThat(accessToken).isNotNull().isNotEmpty();

        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(accessToken)).isEqualTo(testUserId.toString());

        byte[] providerKeyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(DUMMY_JWT_SECRET_BASE64);
        SecretKey providerKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(providerKeyBytes);

        Claims claims = Jwts.parser()
                .verifyWith(providerKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(testUserId.toString());
        assertThat(claims.getIssuedAt()).isNotNull().isBeforeOrEqualTo(new Date());
        assertThat(claims.getExpiration()).isNotNull().isAfter(new Date());

        long expectedExpiryTime = claims.getIssuedAt().getTime() + TEST_ACCESS_EXPIRATION_MS;
        assertThat(claims.getExpiration().getTime()).isEqualTo(expectedExpiryTime);
    }

    @Test
    void generateRefreshToken_shouldGenerateValidToken_whenAuthenticationIsValid() {
        // given
        setupAuthenticationMocks();

        // when
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // then
        assertThat(refreshToken).isNotNull().isNotEmpty();

        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(refreshToken)).isEqualTo(testUserId.toString());

        byte[] providerKeyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(DUMMY_JWT_SECRET_BASE64);
        SecretKey providerKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(providerKeyBytes);

        Claims claims = Jwts.parser()
                .verifyWith(providerKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(testUserId.toString());
        assertThat(claims.getIssuedAt()).isNotNull().isBeforeOrEqualTo(new Date());
        assertThat(claims.getExpiration()).isNotNull().isAfter(new Date());

        long expectedExpiryTime = claims.getIssuedAt().getTime() + TEST_REFRESH_EXPIRATION_MS;
        assertThat(claims.getExpiration().getTime()).isEqualTo(expectedExpiryTime);
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValid() {
        // given
        setupAuthenticationMocks();
        String validToken = jwtTokenProvider.generateAccessToken(authentication);

        // when
        boolean isValid = jwtTokenProvider.validateToken(validToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenHasInvalidSignature() {
        // given
        SecretKey wrongKey = Jwts.SIG.HS512.key().build();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + TEST_ACCESS_EXPIRATION_MS);
        String tokenWithWrongSignature = Jwts.builder()
                .subject(testUserId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(wrongKey, Jwts.SIG.HS512)
                .compact();

        // when
        boolean isValid = jwtTokenProvider.validateToken(tokenWithWrongSignature);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsExpired() {
        // given
        byte[] providerKeyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(DUMMY_JWT_SECRET_BASE64);
        SecretKey providerKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(providerKeyBytes);

        Date now = new Date();
        Date expiryDateInPast = new Date(now.getTime() - TEST_ACCESS_EXPIRATION_MS - 1000);
        Date issuedAtInPast = new Date(expiryDateInPast.getTime() - TEST_ACCESS_EXPIRATION_MS);

        String expiredToken = Jwts.builder()
                .subject(testUserId.toString())
                .issuedAt(issuedAtInPast)
                .expiration(expiryDateInPast)
                .signWith(providerKey, Jwts.SIG.HS512)
                .compact();

        // when
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsMalformed() {
        // given
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // when
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsEmptyOrNull() {
        // given
        String emptyToken = "";

        // when
        boolean isEmptyValid = jwtTokenProvider.validateToken(emptyToken);
        boolean isNullValid = jwtTokenProvider.validateToken(null);

        // then
        assertThat(isEmptyValid).isFalse();
        assertThat(isNullValid).isFalse();
    }

    @Test
    void getUserIdFromToken_shouldReturnUserId_whenTokenIsValid() {
        // given
        setupAuthenticationMocks();
        String validToken = jwtTokenProvider.generateAccessToken(authentication);

        // when
        String userIdFromToken = jwtTokenProvider.getUserIdFromToken(validToken);

        // then
        assertThat(userIdFromToken).isEqualTo(testUserId.toString());
    }

    @Test
    void getUserIdFromToken_shouldThrowException_whenTokenIsInvalid() {
        // given
        // Przypadek 1: Zła sygnatura
        SecretKey wrongKey = Jwts.SIG.HS512.key().build();
        String tokenWithWrongSignature = Jwts.builder()
                .subject(testUserId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TEST_ACCESS_EXPIRATION_MS))
                .signWith(wrongKey, Jwts.SIG.HS512)
                .compact();

        // Przypadek 2: Wygasły token
        byte[] providerKeyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(DUMMY_JWT_SECRET_BASE64);
        SecretKey providerKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(providerKeyBytes);
        String expiredToken = Jwts.builder()
                .subject(testUserId.toString())
                .issuedAt(new Date(System.currentTimeMillis() - 2 * TEST_ACCESS_EXPIRATION_MS))
                .expiration(new Date(System.currentTimeMillis() - TEST_ACCESS_EXPIRATION_MS))
                .signWith(providerKey, Jwts.SIG.HS512)
                .compact();

        // when and then
        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(tokenWithWrongSignature))
                .isInstanceOf(SignatureException.class);

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void generateAccessToken_shouldThrowException_whenPrincipalIsNotUserDetailsImpl() {
        // given
        Authentication wrongAuth = new UsernamePasswordAuthenticationToken("notUserDetailsImpl", "password");

        // when and then
        assertThatThrownBy(() -> jwtTokenProvider.generateAccessToken(wrongAuth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid authentication principal type");
    }

    @Test
    void generateRefreshToken_shouldThrowException_whenPrincipalIsNotUserDetailsImpl() {
        // given
        Authentication wrongAuth = new UsernamePasswordAuthenticationToken("notUserDetailsImpl", "password");

        // when and then
        assertThatThrownBy(() -> jwtTokenProvider.generateRefreshToken(wrongAuth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid authentication principal type");
    }
}