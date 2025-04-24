package pl.jit.flashcards.data.api_model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FlashcardSuggestionApiModel(
    @NotBlank
    @Size(max = 500)
    String frontContent,

    @NotBlank
    @Size(max = 200)
    String backContent
) {} 