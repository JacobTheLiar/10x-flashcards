package pl.jit.flashcards.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.jit.flashcards.data.request.LoginRequest;
import pl.jit.flashcards.data.request.RefreshTokenRequest;
import pl.jit.flashcards.data.request.RegisterRequest;
import pl.jit.flashcards.data.response.LoginResponse;
import pl.jit.flashcards.data.response.RefreshTokenResponse;
import pl.jit.flashcards.data.response.RegisterResponse;
import pl.jit.flashcards.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Received registration request for email: {}", registerRequest.email());
        return authService.register(registerRequest);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received login request for email: {}", loginRequest.email());
        return authService.login(loginRequest);
    }

    @PostMapping("/refresh-token")
    public RefreshTokenResponse refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("Received refresh token request");
        return authService.refreshToken(refreshTokenRequest);
    }
}