package pl.jit.flashcards.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.jit.flashcards.data.FlashcardSourceType;
import pl.jit.flashcards.data.api_model.FlashcardApiModel;
import pl.jit.flashcards.data.request.CreateFlashcardRequest;
import pl.jit.flashcards.data.request.SaveFlashcardsRequest;
import pl.jit.flashcards.data.request.UpdateFlashcardRequest;
import pl.jit.flashcards.data.response.GetAllFlashcardsResponse;
import pl.jit.flashcards.data.response.SaveFlashcardsResponse;
import pl.jit.flashcards.entity.FlashcardEntity;
import pl.jit.flashcards.entity.GenerationEntity;
import pl.jit.flashcards.mapper.FlashcardMapper;
import pl.jit.flashcards.repository.FlashcardRepository;
import pl.jit.flashcards.repository.GenerationRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceImplTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private GenerationRepository generationRepository;

    @Mock
    private FlashcardMapper flashcardMapper;

    @InjectMocks
    private FlashcardServiceImpl flashcardService;

    @Captor
    private ArgumentCaptor<List<FlashcardEntity>> flashcardEntityListCaptor;

    @Captor
    private ArgumentCaptor<FlashcardEntity> flashcardEntityCaptor;

    @Test
    void saveFlashcards_shouldSaveEntitiesAndReturnResponse_whenGenerationExists() {
        UUID generationId = UUID.randomUUID();
        UUID flashcardId1 = UUID.randomUUID();
        UUID flashcardId2 = UUID.randomUUID();
        Instant now = Instant.now();

        CreateFlashcardRequest createRequest1 = new CreateFlashcardRequest("Front 1", "Back 1", FlashcardSourceType.AI_FULL);
        CreateFlashcardRequest createRequest2 = new CreateFlashcardRequest("Front 2", "Back 2", FlashcardSourceType.AI_FULL);
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(generationId, List.of(createRequest1, createRequest2));

        GenerationEntity mockGeneration = new GenerationEntity();
        mockGeneration.setId(generationId);

        FlashcardEntity entityToSave1 = new FlashcardEntity();
        entityToSave1.setFrontContent("Front 1");
        entityToSave1.setBackContent("Back 1");
        entityToSave1.setSourceType(FlashcardSourceType.AI_FULL.getValue());

        FlashcardEntity entityToSave2 = new FlashcardEntity();
        entityToSave2.setFrontContent("Front 2");
        entityToSave2.setBackContent("Back 2");
        entityToSave2.setSourceType(FlashcardSourceType.AI_FULL.getValue());

        FlashcardEntity savedEntity1 = new FlashcardEntity();
        savedEntity1.setId(flashcardId1);
        savedEntity1.setFrontContent("Front 1");
        savedEntity1.setBackContent("Back 1");
        savedEntity1.setSourceType(FlashcardSourceType.AI_FULL.getValue());
        savedEntity1.setGeneration(mockGeneration);
        savedEntity1.setLastModifiedAt(now);

        FlashcardEntity savedEntity2 = new FlashcardEntity();
        savedEntity2.setId(flashcardId2);
        savedEntity2.setFrontContent("Front 2");
        savedEntity2.setBackContent("Back 2");
        savedEntity2.setSourceType(FlashcardSourceType.AI_FULL.getValue());
        savedEntity2.setGeneration(mockGeneration);
        savedEntity2.setLastModifiedAt(now);

        FlashcardApiModel responseApiModel1 = new FlashcardApiModel(flashcardId1, "Front 1", "Back 1", FlashcardSourceType.AI_FULL, now);
        FlashcardApiModel responseApiModel2 = new FlashcardApiModel(flashcardId2, "Front 2", "Back 2", FlashcardSourceType.AI_FULL, now);

        when(generationRepository.findById(generationId)).thenReturn(Optional.of(mockGeneration));
        when(flashcardMapper.fromRequestToEntity(createRequest1)).thenReturn(entityToSave1);
        when(flashcardMapper.fromRequestToEntity(createRequest2)).thenReturn(entityToSave2);
        when(flashcardRepository.saveAll(anyList())).thenReturn(List.of(savedEntity1, savedEntity2));
        when(flashcardMapper.toApiModel(savedEntity1)).thenReturn(responseApiModel1);
        when(flashcardMapper.toApiModel(savedEntity2)).thenReturn(responseApiModel2);

        // when
        SaveFlashcardsResponse response = flashcardService.saveFlashcards(request);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response).isNotNull();
        softly.assertThat(response.savedCount()).isEqualTo(2);
        softly.assertThat(response.flashcards()).containsExactlyInAnyOrder(responseApiModel1, responseApiModel2);

        verify(generationRepository).findById(generationId);
        verify(flashcardMapper).fromRequestToEntity(createRequest1);
        verify(flashcardMapper).fromRequestToEntity(createRequest2);
        verify(flashcardRepository).saveAll(flashcardEntityListCaptor.capture());
        verify(flashcardMapper).toApiModel(savedEntity1);
        verify(flashcardMapper).toApiModel(savedEntity2);

        List<FlashcardEntity> capturedList = flashcardEntityListCaptor.getValue();
        softly.assertThat(capturedList).hasSize(2);
        softly.assertThat(capturedList.get(0).getGeneration()).isEqualTo(mockGeneration);
        softly.assertThat(capturedList.get(0).getLastModifiedAt()).isNotNull(); 
        softly.assertThat(capturedList.get(1).getGeneration()).isEqualTo(mockGeneration);
        softly.assertThat(capturedList.get(1).getLastModifiedAt()).isNotNull();
        softly.assertThat(capturedList.get(0).getFrontContent()).isEqualTo("Front 1");
        softly.assertThat(capturedList.get(1).getFrontContent()).isEqualTo("Front 2");

        softly.assertAll();
    }

    @Test
    void saveFlashcards_shouldThrowEntityNotFoundException_whenGenerationDoesNotExist() {
        // given
        UUID nonExistentGenerationId = UUID.randomUUID();
        CreateFlashcardRequest createRequest = new CreateFlashcardRequest("Q", "A", FlashcardSourceType.AI_FULL);
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(nonExistentGenerationId, List.of(createRequest));

        when(generationRepository.findById(nonExistentGenerationId)).thenReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> flashcardService.saveFlashcards(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Generation not found with id: " + nonExistentGenerationId);

        // verify
        verify(generationRepository).findById(nonExistentGenerationId);
        verify(flashcardMapper, never()).fromRequestToEntity(any(CreateFlashcardRequest.class));
        verify(flashcardRepository, never()).saveAll(anyList());
    }

    @Test
    void getAllFlashcards_shouldReturnAllFlashcards_whenFlashcardsExist() {
        // given
        FlashcardEntity entity1 = new FlashcardEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setFrontContent("Front 1");
        FlashcardEntity entity2 = new FlashcardEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setFrontContent("Front 2");
        List<FlashcardEntity> mockEntities = List.of(entity1, entity2);

        FlashcardApiModel apiModel1 = new FlashcardApiModel(entity1.getId(), "Front 1", "Back 1", FlashcardSourceType.MANUAL, Instant.now());
        FlashcardApiModel apiModel2 = new FlashcardApiModel(entity2.getId(), "Front 2", "Back 2", FlashcardSourceType.AI_FULL, Instant.now());
        List<FlashcardApiModel> mockApiModels = List.of(apiModel1, apiModel2);

        when(flashcardRepository.findAll()).thenReturn(mockEntities);
        when(flashcardMapper.toApiModelList(mockEntities)).thenReturn(mockApiModels);

        // when
        GetAllFlashcardsResponse actualResponse = flashcardService.getAllFlashcards();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualResponse).isNotNull();
        softly.assertThat(actualResponse.flashcards()).hasSize(2);
        softly.assertThat(actualResponse.flashcards()).containsExactlyInAnyOrderElementsOf(mockApiModels);

        verify(flashcardRepository).findAll();
        verify(flashcardMapper).toApiModelList(mockEntities);

        softly.assertAll();
    }

    @Test
    void getAllFlashcards_shouldReturnEmptyList_whenNoFlashcardsExist() {
        // given
        List<FlashcardEntity> emptyEntityList = List.of();
        List<FlashcardApiModel> emptyApiModelList = List.of();

        when(flashcardRepository.findAll()).thenReturn(emptyEntityList);
        when(flashcardMapper.toApiModelList(emptyEntityList)).thenReturn(emptyApiModelList);

        // when
        GetAllFlashcardsResponse actualResponse = flashcardService.getAllFlashcards();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualResponse).isNotNull();
        softly.assertThat(actualResponse.flashcards()).isNotNull();
        softly.assertThat(actualResponse.flashcards()).isEmpty();

        // verify
        verify(flashcardRepository).findAll();
        verify(flashcardMapper).toApiModelList(emptyEntityList);

        softly.assertAll();
    }

    @Test
    void updateFlashcard_shouldUpdateAndReturnApiModel_whenFlashcardExists() {
        // given
        UUID flashcardId = UUID.randomUUID();
        String originalFront = "Original Front";
        String originalBack = "Original Back";
        String updatedFront = "Updated Front";
        String updatedBack = "Updated Back";
        Instant timeBeforeUpdate = Instant.now().minusSeconds(10);
        Instant timeAfterUpdate = Instant.now();

        UpdateFlashcardRequest request = new UpdateFlashcardRequest(updatedFront, updatedBack);

        FlashcardEntity existingEntity = new FlashcardEntity();
        existingEntity.setId(flashcardId);
        existingEntity.setFrontContent(originalFront);
        existingEntity.setBackContent(originalBack);
        existingEntity.setSourceType(FlashcardSourceType.AI_FULL.getValue());
        existingEntity.setLastModifiedAt(timeBeforeUpdate);

        FlashcardEntity savedEntity = new FlashcardEntity();
        savedEntity.setId(flashcardId);
        savedEntity.setFrontContent(updatedFront);
        savedEntity.setBackContent(updatedBack);
        savedEntity.setSourceType(FlashcardSourceType.AI_EDITED.getValue());
        savedEntity.setLastModifiedAt(timeAfterUpdate);

        FlashcardApiModel expectedApiModel = new FlashcardApiModel(flashcardId, updatedFront, updatedBack, FlashcardSourceType.AI_EDITED, timeAfterUpdate);

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(existingEntity));
        when(flashcardRepository.save(any(FlashcardEntity.class))).thenReturn(savedEntity);
        when(flashcardMapper.toApiModel(savedEntity)).thenReturn(expectedApiModel);

        // when
        FlashcardApiModel actualApiModel = flashcardService.updateFlashcard(flashcardId, request);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actualApiModel).isEqualTo(expectedApiModel);

        // verify
        verify(flashcardRepository).findById(flashcardId);
        verify(flashcardRepository).save(flashcardEntityCaptor.capture());
        verify(flashcardMapper).toApiModel(savedEntity);

        FlashcardEntity capturedEntity = flashcardEntityCaptor.getValue();
        softly.assertThat(capturedEntity.getId()).isEqualTo(flashcardId);
        softly.assertThat(capturedEntity.getFrontContent()).isEqualTo(updatedFront);
        softly.assertThat(capturedEntity.getBackContent()).isEqualTo(updatedBack);
        softly.assertThat(capturedEntity.getSourceType()).isEqualTo(FlashcardSourceType.AI_EDITED.getValue());
        softly.assertThat(capturedEntity.getLastModifiedAt()).isNotNull();
        softly.assertThat(capturedEntity.getLastModifiedAt()).isNotEqualTo(timeBeforeUpdate);

        softly.assertAll();
    }

    @Test
    void updateFlashcard_shouldThrowEntityNotFoundException_whenFlashcardDoesNotExist() {
        // given
        UUID nonExistentFlashcardId = UUID.randomUUID();
        UpdateFlashcardRequest request = new UpdateFlashcardRequest("New Front", "New Back");

        when(flashcardRepository.findById(nonExistentFlashcardId)).thenReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> flashcardService.updateFlashcard(nonExistentFlashcardId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Flashcard not found with id: " + nonExistentFlashcardId);

        // verify
        verify(flashcardRepository).findById(nonExistentFlashcardId);
        verify(flashcardRepository, never()).save(any(FlashcardEntity.class));
        verify(flashcardMapper, never()).toApiModel(any(FlashcardEntity.class));
    }

    @Test
    void deleteFlashcard_shouldCallDeleteById_whenFlashcardExists() {
        // given
        UUID flashcardId = UUID.randomUUID();

        when(flashcardRepository.existsById(flashcardId)).thenReturn(true);

        // when
        flashcardService.deleteFlashcard(flashcardId);

        // then
        verify(flashcardRepository).existsById(flashcardId);
        verify(flashcardRepository).deleteById(flashcardId);
    }

    @Test
    void deleteFlashcard_shouldThrowEntityNotFoundException_whenFlashcardDoesNotExist() {
        // given
        UUID nonExistentFlashcardId = UUID.randomUUID();

        when(flashcardRepository.existsById(nonExistentFlashcardId)).thenReturn(false);

        // when and then
        assertThatThrownBy(() -> flashcardService.deleteFlashcard(nonExistentFlashcardId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Flashcard not found with id: " + nonExistentFlashcardId);

        // verify
        verify(flashcardRepository).existsById(nonExistentFlashcardId);
        verify(flashcardRepository, never()).deleteById(any(UUID.class));
    }
}