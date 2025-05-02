package pl.jit.flashcards.service;

import pl.jit.flashcards.data.request.LoginRequest;
import pl.jit.flashcards.data.request.RefreshTokenRequest;
import pl.jit.flashcards.data.request.RegisterRequest;
import pl.jit.flashcards.data.response.LoginResponse;
import pl.jit.flashcards.data.response.RefreshTokenResponse;
import pl.jit.flashcards.data.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest registerRequest);

    LoginResponse login(LoginRequest loginRequest);

    RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}