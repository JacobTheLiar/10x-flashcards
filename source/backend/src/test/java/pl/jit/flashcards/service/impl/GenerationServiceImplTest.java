package pl.jit.flashcards.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jit.flashcards.data.api_model.FlashcardSuggestionApiModel;
import pl.jit.flashcards.data.request.GenerateFlashcardsRequest;
import pl.jit.flashcards.data.response.GenerateFlashcardsResponse;
import pl.jit.flashcards.entity.GenerationEntity;
import pl.jit.flashcards.repository.GenerationRepository;
import pl.jit.flashcards.service.AiIntegrationService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerationServiceImplTest {

    @Mock
    private AiIntegrationService aiIntegrationService;

    @Mock
    private GenerationRepository generationRepository;

    @InjectMocks
    private GenerationServiceImpl generationService;

    @Captor
    private ArgumentCaptor<GenerationEntity> generationEntityCaptor;

    private static final int MAX_SOURCE_TEXT_LENGTH = 10000;

    @Test
    void generateFlashcards_shouldReturnResponseAndSaveRecord_whenRequestIsValid() {
        // given
        String sourceText = "This is a valid source text.";
        GenerateFlashcardsRequest request = new GenerateFlashcardsRequest(sourceText);

        FlashcardSuggestionApiModel suggestion1 = new FlashcardSuggestionApiModel("Q1", "A1");
        FlashcardSuggestionApiModel suggestion2 = new FlashcardSuggestionApiModel("Q2", "A2");
        List<FlashcardSuggestionApiModel> mockSuggestions = List.of(suggestion1, suggestion2);

        UUID generationId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        GenerationEntity savedGenerationEntity = new GenerationEntity();
        savedGenerationEntity.setId(generationId);
        savedGenerationEntity.setCreatedAt(createdAt);

        when(aiIntegrationService.generateSuggestions(sourceText)).thenReturn(mockSuggestions);
        when(generationRepository.save(any(GenerationEntity.class))).thenReturn(savedGenerationEntity);

        // when
        GenerateFlashcardsResponse response = generationService.generateFlashcards(request);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response).isNotNull();
        softly.assertThat(response.generationId()).isEqualTo(generationId);
        softly.assertThat(response.createdAt()).isEqualTo(createdAt);
        softly.assertThat(response.generationTimeMs()).isNotNegative();
        softly.assertThat(response.suggestedFlashcards()).isEqualTo(mockSuggestions);

        // verify
        verify(aiIntegrationService).generateSuggestions(sourceText);
        verify(generationRepository).save(generationEntityCaptor.capture());

        GenerationEntity capturedEntity = generationEntityCaptor.getValue();
        softly.assertThat(capturedEntity.getSourceTextMd5()).isEqualTo(DigestUtils.md5Hex(sourceText));
        softly.assertThat(capturedEntity.getSourceTextLength()).isEqualTo(sourceText.length());
        softly.assertThat(capturedEntity.getSuggestedFlashcardsCount()).isEqualTo(mockSuggestions.size());
        softly.assertThat(capturedEntity.getGenerationTimeMs()).isNotNegative();
        softly.assertThat(capturedEntity.getCreatedAt()).isNotNull();

        softly.assertAll();
    }

    @Test
    void generateFlashcards_shouldThrowIllegalArgumentException_whenSourceTextIsEmpty() {
        // given
        String emptySourceText = "";
        GenerateFlashcardsRequest request = new GenerateFlashcardsRequest(emptySourceText);

        // when and then
        assertThatThrownBy(() -> generationService.generateFlashcards(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source text cannot be empty.");

        // verify
        verify(aiIntegrationService, never()).generateSuggestions(any());
        verify(generationRepository, never()).save(any());
    }

    @Test
    void generateFlashcards_shouldThrowIllegalArgumentException_whenSourceTextIsNull() {
        // given
        GenerateFlashcardsRequest request = new GenerateFlashcardsRequest(null);

        // when and then
        assertThatThrownBy(() -> generationService.generateFlashcards(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source text cannot be empty.");

        // verify
        verify(aiIntegrationService, never()).generateSuggestions(any());
        verify(generationRepository, never()).save(any());
    }


    @Test
    void generateFlashcards_shouldThrowIllegalArgumentException_whenSourceTextIsTooLong() {
        // given
        String longSourceText = "a".repeat(MAX_SOURCE_TEXT_LENGTH + 1);
        GenerateFlashcardsRequest request = new GenerateFlashcardsRequest(longSourceText);

        // when and then
        assertThatThrownBy(() -> generationService.generateFlashcards(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source text exceeds maximum length of " + MAX_SOURCE_TEXT_LENGTH + " characters.");

        // verify
        verify(aiIntegrationService, never()).generateSuggestions(any());
        verify(generationRepository, never()).save(any());
    }
}