package pl.jit.flashcards.service.impl;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jit.flashcards.data.FlashcardSourceType;
import pl.jit.flashcards.data.api_model.FlashcardApiModel;
import pl.jit.flashcards.data.response.StartReviewSessionResponse;
import pl.jit.flashcards.entity.FlashcardEntity;
import pl.jit.flashcards.mapper.FlashcardMapper;
import pl.jit.flashcards.repository.FlashcardRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewSessionServiceImplTest {

    private static final int DEFAULT_REVIEW_SIZE = 10;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private FlashcardMapper flashcardMapper;

    @InjectMocks
    private ReviewSessionServiceImpl reviewSessionService;

    @Test
    void startReviewSession_shouldReturnFlashcards_whenFlashcardsExist() {
        FlashcardEntity entity1 = new FlashcardEntity();
        entity1.setId(UUID.randomUUID());
        FlashcardEntity entity2 = new FlashcardEntity();
        entity2.setId(UUID.randomUUID());
        List<FlashcardEntity> mockEntities = List.of(entity1, entity2);

        FlashcardApiModel apiModel1 = new FlashcardApiModel(entity1.getId(), "Q1", "A1", FlashcardSourceType.MANUAL, Instant.now());
        FlashcardApiModel apiModel2 = new FlashcardApiModel(entity2.getId(), "Q2", "A2", FlashcardSourceType.AI_FULL, Instant.now());
        List<FlashcardApiModel> mockApiModels = List.of(apiModel1, apiModel2);

        when(flashcardRepository.findRandomFlashcards(DEFAULT_REVIEW_SIZE)).thenReturn(mockEntities);
        when(flashcardMapper.toApiModelList(mockEntities)).thenReturn(mockApiModels);

        StartReviewSessionResponse actualResponse = reviewSessionService.startReviewSession();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualResponse).isNotNull();
        softly.assertThat(actualResponse.flashcards()).isEqualTo(mockApiModels);

        verify(flashcardRepository).findRandomFlashcards(DEFAULT_REVIEW_SIZE);
        verify(flashcardMapper).toApiModelList(mockEntities);

        softly.assertAll();
    }

    @Test
    void startReviewSession_shouldReturnEmptyList_whenNoFlashcardsExist() {
        List<FlashcardEntity> emptyEntityList = List.of();
        List<FlashcardApiModel> emptyApiModelList = List.of();

        when(flashcardRepository.findRandomFlashcards(DEFAULT_REVIEW_SIZE)).thenReturn(emptyEntityList);
        when(flashcardMapper.toApiModelList(emptyEntityList)).thenReturn(emptyApiModelList);

        StartReviewSessionResponse actualResponse = reviewSessionService.startReviewSession();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualResponse).isNotNull();
        softly.assertThat(actualResponse.flashcards()).isNotNull();
        softly.assertThat(actualResponse.flashcards()).isEmpty();

        verify(flashcardRepository).findRandomFlashcards(DEFAULT_REVIEW_SIZE);
        verify(flashcardMapper).toApiModelList(emptyEntityList);

        softly.assertAll();
    }
}