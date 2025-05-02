package pl.jit.flashcards.data.response;

import pl.jit.flashcards.data.api_model.FlashcardApiModel;

import java.util.List;

/**
 * DTO for the response when starting a review session.
 *
 * @param flashcards The list of flashcards selected for the review session.
 */
public record StartReviewSessionResponse(
        List<FlashcardApiModel> flashcards
) {
}
