package pl.jit.flashcards.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateFlashcardsRequest(
    @NotBlank
    @Size(max = 10000, message = "Source text must not exceed 10000 characters")
    String sourceText
) {} 