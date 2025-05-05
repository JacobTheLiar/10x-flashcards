package pl.jit.flashcards.service.impl;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.jit.flashcards.data.request.LoginRequest;
import pl.jit.flashcards.data.request.RegisterRequest;
import pl.jit.flashcards.data.request.RefreshTokenRequest;
import pl.jit.flashcards.data.response.LoginResponse;
import pl.jit.flashcards.data.response.RegisterResponse;
import pl.jit.flashcards.data.response.RefreshTokenResponse;
import pl.jit.flashcards.exception.InvalidTokenException;
import pl.jit.flashcards.exception.UserAlreadyExistsException;
import pl.jit.flashcards.entity.UserEntity;
import pl.jit.flashcards.repository.UserRepository;
import pl.jit.flashcards.security.JwtTokenProvider;
import pl.jit.flashcards.security.UserDetailsImpl;
import pl.jit.flashcards.security.UserDetailsServiceImpl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<UserEntity> userEntityCaptor;

    @Test
    void register_shouldSaveUserAndReturnResponse_whenEmailIsNotTaken() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String hashedPassword = "hashedPassword";
        UUID userId = UUID.randomUUID();
        Instant creationTime = Instant.now();
        RegisterRequest request = new RegisterRequest(email, password);

        UserEntity mockSavedUser = new UserEntity();
        mockSavedUser.setId(userId);
        mockSavedUser.setEmail(email);
        mockSavedUser.setPasswordHash(hashedPassword);
        mockSavedUser.setCreatedAt(creationTime);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(userRepository.saveAndFlush(any(UserEntity.class))).thenReturn(mockSavedUser);

        // when
        RegisterResponse response = authService.register(request);

        // then
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(response).isNotNull();
        softly.assertThat(response.id()).isEqualTo(userId);
        softly.assertThat(response.email()).isEqualTo(email);
        softly.assertThat(response.createdAt()).isEqualTo(creationTime);

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).saveAndFlush(userEntityCaptor.capture());

        UserEntity capturedUser = userEntityCaptor.getValue();
        softly.assertThat(capturedUser).isNotNull();
        softly.assertThat(capturedUser.getEmail()).isEqualTo(email);
        softly.assertThat(capturedUser.getPasswordHash()).isEqualTo(hashedPassword);
        softly.assertThat(capturedUser.getId()).isNull();
        softly.assertThat(capturedUser.getCreatedAt()).isNull();

        softly.assertAll();
    }

    @Test
    void register_shouldThrowUserAlreadyExistsException_whenEmailIsTaken() {
        // given
        String email = "existing@example.com";
        String password = "password123";
        RegisterRequest request = new RegisterRequest(email, password);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email address already in use: " + email);

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).saveAndFlush(any(UserEntity.class));
    }

    @Test
    void login_shouldReturnTokensAndSetLastLogin_whenCredentialsAreValid() {
        // given
        String email = "user@example.com";
        String password = "password123";
        UUID userId = UUID.randomUUID();
        String accessToken = "dummyAccessToken";
        String refreshToken = "dummyRefreshToken";
        long expirationMs = 3600000; // 1 hour
        int expirationSeconds = 3600;

        LoginRequest loginRequest = new LoginRequest(email, password);
        UserDetailsImpl userDetails = new UserDetailsImpl(userId, email, "hashedPassword", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication successfulAuth = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(successfulAuth);
        when(jwtTokenProvider.generateAccessToken(successfulAuth)).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(successfulAuth)).thenReturn(refreshToken);
        when(jwtTokenProvider.getJwtExpirationAccessMs()).thenReturn(expirationMs);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(userEntity));

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response).isNotNull();
        softly.assertThat(response.accessToken()).isEqualTo(accessToken);
        softly.assertThat(response.refreshToken()).isEqualTo(refreshToken);
        softly.assertThat(response.expiresIn()).isEqualTo(expirationSeconds);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateAccessToken(successfulAuth);
        verify(jwtTokenProvider).generateRefreshToken(successfulAuth);
        verify(userRepository).findById(userId);
        verify(userRepository).save(userEntityCaptor.capture());

        Authentication securityContextAuth = SecurityContextHolder.getContext().getAuthentication();
        UserEntity capturedUser = userEntityCaptor.getValue();
        softly.assertThat(capturedUser.getLastLoginAt()).isNotNull();
        softly.assertThat(securityContextAuth).isEqualTo(successfulAuth);
        softly.assertAll();
    }

    @Test
    void login_shouldThrowAuthenticationException_whenCredentialsAreInvalid() {
        // given
        String email = "user@example.com";
        String password = "wrongPassword";
        LoginRequest loginRequest = new LoginRequest(email, password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        // when / then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(org.springframework.security.core.AuthenticationException.class);

        // verify
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authRequestCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authRequestCaptor.capture());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(authRequestCaptor.getValue().getName()).isEqualTo(email);
        softly.assertThat(authRequestCaptor.getValue().getCredentials()).isEqualTo(password);
        softly.assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        softly.assertAll();

        verify(jwtTokenProvider, never()).generateAccessToken(any());
        verify(jwtTokenProvider, never()).generateRefreshToken(any());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());

        SecurityContextHolder.clearContext();
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenIsValid() {
        // given
        String validRefreshToken = "validRefreshToken";
        UUID userId = UUID.randomUUID();
        String userIdStr = userId.toString();
        String newAccessToken = "newAccessToken";
        long expirationMs = 1800000; // 30 minutes
        int expirationSeconds = 1800;

        RefreshTokenRequest request = new RefreshTokenRequest(validRefreshToken);

        UserDetailsImpl mockUserDetails = mock(UserDetailsImpl.class);
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(mockUserDetails).getAuthorities();

        when(jwtTokenProvider.validateToken(validRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validRefreshToken)).thenReturn(userIdStr);
        when(userDetailsService.loadUserById(userId)).thenReturn(mockUserDetails);
        ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
        when(jwtTokenProvider.generateAccessToken(authCaptor.capture())).thenReturn(newAccessToken);
        when(jwtTokenProvider.getJwtExpirationAccessMs()).thenReturn(expirationMs);

        // when
        RefreshTokenResponse response = authService.refreshToken(request);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response).isNotNull();
        softly.assertThat(response.accessToken()).isEqualTo(newAccessToken);
        softly.assertThat(response.expiresIn()).isEqualTo(expirationSeconds);

        verify(jwtTokenProvider).validateToken(validRefreshToken);
        verify(jwtTokenProvider).getUserIdFromToken(validRefreshToken);
        verify(userDetailsService).loadUserById(userId);
        verify(jwtTokenProvider).generateAccessToken(any(Authentication.class));
        verify(jwtTokenProvider).getJwtExpirationAccessMs();

        Authentication capturedAuth = authCaptor.getValue();
        softly.assertThat(capturedAuth.getPrincipal()).isEqualTo(mockUserDetails);
        softly.assertThat(capturedAuth.getCredentials()).isNull();
        softly.assertThat(capturedAuth.getAuthorities())
                .hasSize(1)
                .first()
                .isEqualTo(new SimpleGrantedAuthority("ROLE_USER"));

        softly.assertAll();
    }

    @Test
    void refreshToken_shouldThrowInvalidTokenException_whenTokenIsInvalid() {
        // given
        String invalidRefreshToken = "invalidOrExpiredToken";
        RefreshTokenRequest request = new RefreshTokenRequest(invalidRefreshToken);

        when(jwtTokenProvider.validateToken(invalidRefreshToken)).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");

        // verify
        verify(jwtTokenProvider).validateToken(invalidRefreshToken);
        verify(jwtTokenProvider, never()).getUserIdFromToken(anyString());
        verify(userDetailsService, never()).loadUserById(any(UUID.class));
        verify(jwtTokenProvider, never()).generateAccessToken(any(Authentication.class));
    }
}