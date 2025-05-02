package pl.jit.flashcards.service;

import pl.jit.flashcards.data.request.GenerateFlashcardsRequest;
import pl.jit.flashcards.data.response.GenerateFlashcardsResponse;

public interface GenerationService {

    GenerateFlashcardsResponse generateFlashcards(GenerateFlashcardsRequest request);
}
