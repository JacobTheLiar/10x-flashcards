package pl.jit.flashcards.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFlashcardRequest(
    @NotBlank
    @Size(max = 500)
    String frontContent,

    @NotBlank
    @Size(max = 200)
    String backContent
) {} 