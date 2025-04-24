package pl.jit.flashcards.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.jit.flashcards.data.FlashcardSourceType;

public record CreateFlashcardRequest(
    @NotBlank
    @Size(max = 500)
    String frontContent,

    @NotBlank
    @Size(max = 200)
    String backContent,

    @NotNull
    FlashcardSourceType sourceType
) {} 