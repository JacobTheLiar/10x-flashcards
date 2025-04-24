package pl.jit.flashcards.data.response;

import pl.jit.flashcards.data.api_model.FlashcardApiModel;

import java.util.List;

public record SaveFlashcardsResponse(
    Integer savedCount,
    List<FlashcardApiModel> flashcards
) {} 