package pl.jit.flashcards.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jit.flashcards.data.api_model.FlashcardApiModel;
import pl.jit.flashcards.data.response.StartReviewSessionResponse;
import pl.jit.flashcards.entity.FlashcardEntity;
import pl.jit.flashcards.mapper.FlashcardMapper;
import pl.jit.flashcards.repository.FlashcardRepository;
import pl.jit.flashcards.service.ReviewSessionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewSessionServiceImpl implements ReviewSessionService {

    private static final int DEFAULT_REVIEW_SIZE = 10;

    private final FlashcardRepository flashcardRepository;
    private final FlashcardMapper flashcardMapper;

    @Override
    @Transactional(readOnly = true)
    public StartReviewSessionResponse startReviewSession() {
        log.info("Starting a new review session for the current user.");

        List<FlashcardEntity> randomFlashcards = flashcardRepository.findRandomFlashcards(DEFAULT_REVIEW_SIZE);

        if (randomFlashcards.isEmpty()) {
            log.warn("No flashcards found for the current user to start a review session.");
        }

        List<FlashcardApiModel> flashcardApis = flashcardMapper.toApiModelList(randomFlashcards);

        log.info("Returning {} flashcards for the review session.", flashcardApis.size());
        return new StartReviewSessionResponse(flashcardApis);
    }
}
