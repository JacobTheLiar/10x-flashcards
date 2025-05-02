package pl.jit.flashcards.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.jit.flashcards.data.FlashcardSourceType;
import pl.jit.flashcards.data.api_model.FlashcardApiModel;
import pl.jit.flashcards.data.request.SaveFlashcardsRequest;
import pl.jit.flashcards.data.request.UpdateFlashcardRequest;
import pl.jit.flashcards.data.response.GetAllFlashcardsResponse;
import pl.jit.flashcards.data.response.SaveFlashcardsResponse;
import pl.jit.flashcards.entity.FlashcardEntity;
import pl.jit.flashcards.entity.GenerationEntity;
import pl.jit.flashcards.mapper.FlashcardMapper;
import pl.jit.flashcards.repository.FlashcardRepository;
import pl.jit.flashcards.repository.GenerationRepository;
import pl.jit.flashcards.service.FlashcardService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final GenerationRepository generationRepository;
    private final FlashcardMapper flashcardMapper;

    @Override
    @Transactional
    public SaveFlashcardsResponse saveFlashcards(SaveFlashcardsRequest request) {
        UUID generationId = request.generationId();

        GenerationEntity generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new EntityNotFoundException("Generation not found with id: " + generationId));

        List<FlashcardEntity> entitiesToSave = request.flashcards().stream()
                .map(apiModel -> {
                    FlashcardEntity entity = flashcardMapper.fromRequestToEntity(apiModel);
                    entity.setGeneration(generation);
                    entity.setLastModifiedAt(Instant.now());
                    return entity;
                })
                .toList();

        List<FlashcardEntity> savedEntities = flashcardRepository.saveAll(entitiesToSave);

        log.info("Saved {} flashcards for generationId: {}", savedEntities.size(), generationId);

        List<FlashcardApiModel> savedFlashcardApis = savedEntities.stream()
                .map(flashcardMapper::toApiModel)
                .toList();

        return new SaveFlashcardsResponse(savedEntities.size(), savedFlashcardApis);
    }

    @Override
    @Transactional(readOnly = true)
    public GetAllFlashcardsResponse getAllFlashcards() {
        log.info("Retrieving all flashcards");
        List<FlashcardEntity> allFlashcards = flashcardRepository.findAll();
        List<FlashcardApiModel> flashcardApis = flashcardMapper.toApiModelList(allFlashcards);
        log.info("Found {} flashcards", flashcardApis.size());
        return new GetAllFlashcardsResponse(flashcardApis);
    }

    @Override
    @Transactional
    public FlashcardApiModel updateFlashcard(UUID flashcardId, UpdateFlashcardRequest request) {
        log.info("Updating flashcard with id: {}", flashcardId);
        FlashcardEntity flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new EntityNotFoundException("Flashcard not found with id: " + flashcardId));

        flashcard.setFrontContent(request.frontContent());
        flashcard.setBackContent(request.backContent());
        flashcard.setSourceType(FlashcardSourceType.AI_EDITED.getValue());
        flashcard.setLastModifiedAt(Instant.now());

        FlashcardEntity updatedFlashcard = flashcardRepository.save(flashcard);
        log.info("Flashcard {} updated successfully", flashcardId);
        return flashcardMapper.toApiModel(updatedFlashcard);
    }

    @Override
    @Transactional
    public void deleteFlashcard(UUID flashcardId) {
        log.info("Deleting flashcard with id: {}", flashcardId);
        if (!flashcardRepository.existsById(flashcardId)) {
            throw new EntityNotFoundException("Flashcard not found with id: " + flashcardId);
        }
        flashcardRepository.deleteById(flashcardId);
        log.info("Flashcard {} deleted successfully", flashcardId);
    }
}