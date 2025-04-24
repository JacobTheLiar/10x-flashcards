package pl.jit.flashcards.data.response;

public record RefreshTokenResponse(
    String accessToken,
    Integer expiresIn
) {} 