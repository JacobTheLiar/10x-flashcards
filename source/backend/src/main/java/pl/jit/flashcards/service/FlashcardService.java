package pl.jit.flashcards.service;

import pl.jit.flashcards.data.request.SaveFlashcardsRequest;
import pl.jit.flashcards.data.request.UpdateFlashcardRequest;
import pl.jit.flashcards.data.response.SaveFlashcardsResponse;
import pl.jit.flashcards.data.response.GetAllFlashcardsResponse;
import pl.jit.flashcards.data.api_model.FlashcardApiModel;

import java.util.UUID;

public interface FlashcardService {

    SaveFlashcardsResponse saveFlashcards(SaveFlashcardsRequest request);

    GetAllFlashcardsResponse getAllFlashcards();

    FlashcardApiModel updateFlashcard(UUID flashcardId, UpdateFlashcardRequest request);

    void deleteFlashcard(UUID flashcardId);
}