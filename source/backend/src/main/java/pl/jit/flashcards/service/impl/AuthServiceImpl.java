package pl.jit.flashcards.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jit.flashcards.data.request.LoginRequest;
import pl.jit.flashcards.data.request.RefreshTokenRequest;
import pl.jit.flashcards.data.request.RegisterRequest;
import pl.jit.flashcards.data.response.LoginResponse;
import pl.jit.flashcards.data.response.RefreshTokenResponse;
import pl.jit.flashcards.data.response.RegisterResponse;
import pl.jit.flashcards.entity.UserEntity;
import pl.jit.flashcards.exception.InvalidTokenException;
import pl.jit.flashcards.exception.UserAlreadyExistsException;
import pl.jit.flashcards.repository.UserRepository;
import pl.jit.flashcards.security.JwtTokenProvider;
import pl.jit.flashcards.security.UserDetailsImpl;
import pl.jit.flashcards.security.UserDetailsServiceImpl;
import pl.jit.flashcards.service.AuthService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("Attempting registration for user: {}", registerRequest.email());

        if (userRepository.existsByEmail(registerRequest.email())) {
            log.warn("Registration failed: Email already exists - {}", registerRequest.email());
            throw new UserAlreadyExistsException("Email address already in use: " + registerRequest.email());
        }

        UserEntity newUser = new UserEntity();
        newUser.setEmail(registerRequest.email());
        newUser.setPasswordHash(passwordEncoder.encode(registerRequest.password()));

        UserEntity savedUser = userRepository.save(newUser);
        log.info("User successfully registered with ID: {}", savedUser.getId());

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getCreatedAt());
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.email());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            UUID userId = userDetails.getId();
            userRepository.findById(userId).ifPresent(user -> {
                user.setLastLoginAt(Instant.now());
                userRepository.save(user);
                log.debug("Updated lastLoginAt for user ID: {}", userId);
            });
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("User successfully authenticated: {}", loginRequest.email());

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        long expirationMs = jwtTokenProvider.getJwtExpirationAccessMs();
        int expirationSeconds = (int) (expirationMs / 1000);
        return new LoginResponse(accessToken, refreshToken, expirationSeconds);
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        log.info("Attempting to refresh token");
        String requestRefreshToken = refreshTokenRequest.refreshToken();

        if (requestRefreshToken == null || !jwtTokenProvider.validateToken(requestRefreshToken)) {
            log.warn("Refresh token validation failed");
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        String userIdStr = jwtTokenProvider.getUserIdFromToken(requestRefreshToken);
        UUID userId = UUID.fromString(userIdStr);
        log.debug("Refresh token validated for user ID: {}", userId);

        UserDetails userDetails = userDetailsService.loadUserById(userId);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        log.info("New access token generated for user ID: {}", userId);

        long expirationMs = jwtTokenProvider.getJwtExpirationAccessMs();
        int expirationSeconds = (int) (expirationMs / 1000);

        return new RefreshTokenResponse(newAccessToken, expirationSeconds);
    }
}