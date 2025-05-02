package pl.jit.flashcards.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.jit.flashcards.data.api_model.FlashcardSuggestionApiModel;
import pl.jit.flashcards.service.AiIntegrationService;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class MockAiIntegrationService implements AiIntegrationService {

    @Override
    public List<FlashcardSuggestionApiModel> generateSuggestions(String sourceText) {
        log.info("Generating mock flashcard suggestions for text starting with: '{}'...",
                sourceText.substring(0, Math.min(sourceText.length(), 50)));

        // Simulate processing time
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("AI generation simulation interrupted");
            return List.of();
        }

        return List.of(
                new FlashcardSuggestionApiModel("Mock Question 1 from AI?", "Mock Answer 1."),
                new FlashcardSuggestionApiModel("Mock Question 2?", "Mock Answer 2: True."),
                new FlashcardSuggestionApiModel("Mock Question 3 Term", "Mock Definition 3.")
        );
    }
}
