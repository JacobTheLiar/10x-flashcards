package pl.jit.flashcards.data.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank
    String refreshToken
) {} 