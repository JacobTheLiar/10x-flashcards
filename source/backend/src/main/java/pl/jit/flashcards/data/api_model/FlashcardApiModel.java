package pl.jit.flashcards.data.api_model;

import pl.jit.flashcards.data.FlashcardSourceType;

import java.time.Instant;
import java.util.UUID;

public record FlashcardApiModel(
    UUID id,
    String frontContent,
    String backContent,
    FlashcardSourceType sourceType,
    Instant lastModifiedAt
) {} 