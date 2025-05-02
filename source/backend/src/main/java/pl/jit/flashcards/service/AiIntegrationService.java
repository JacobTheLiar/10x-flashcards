package pl.jit.flashcards.service;

import pl.jit.flashcards.data.api_model.FlashcardSuggestionApiModel;

import java.util.List;

public interface AiIntegrationService {

    List<FlashcardSuggestionApiModel> generateSuggestions(String sourceText);
}
