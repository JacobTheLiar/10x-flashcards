package pl.jit.flashcards.data.response;

import pl.jit.flashcards.data.api_model.FlashcardApiModel;

import java.util.List;

public record ReviewSessionResponse(
    List<FlashcardApiModel> flashcards
) {} 