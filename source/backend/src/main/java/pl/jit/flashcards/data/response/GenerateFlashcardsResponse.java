package pl.jit.flashcards.data.response;

import pl.jit.flashcards.data.api_model.FlashcardSuggestionApiModel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GenerateFlashcardsResponse(
    UUID generationId,
    Instant createdAt,
    Long generationTimeMs,
    List<FlashcardSuggestionApiModel> suggestedFlashcards
) {} 