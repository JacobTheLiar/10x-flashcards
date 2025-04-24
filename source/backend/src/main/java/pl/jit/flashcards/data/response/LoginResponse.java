package pl.jit.flashcards.data.response;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    Integer expiresIn
) {} 