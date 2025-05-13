package pl.jit.flashcards.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.jit.flashcards.data.api_model.FlashcardApiModel;
import pl.jit.flashcards.data.request.SaveFlashcardsRequest;
import pl.jit.flashcards.data.request.UpdateFlashcardRequest;
import pl.jit.flashcards.data.response.GetAllFlashcardsResponse;
import pl.jit.flashcards.data.response.SaveFlashcardsResponse;
import pl.jit.flashcards.service.FlashcardService;

import java.util.UUID;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@Slf4j
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SaveFlashcardsResponse saveFlashcards(@Valid @RequestBody SaveFlashcardsRequest request) {
        log.info("Received request to save {} flashcards for generationId: {}", request.flashcards().size(), request.generationId());
        return flashcardService.saveFlashcards(request);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public GetAllFlashcardsResponse getAllUserFlashcards() {
        log.info("Received request to get all flashcards for the user.");
        return flashcardService.getAllFlashcards();
    }

    @PutMapping(value = "/{flashcardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FlashcardApiModel updateFlashcard(@PathVariable UUID flashcardId, @Valid @RequestBody UpdateFlashcardRequest request) {
        log.info("Received request to update flashcard with id: {}", flashcardId);
        return flashcardService.updateFlashcard(flashcardId, request);
    }

    @DeleteMapping(value = "/{flashcardId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteFlashcard(@PathVariable UUID flashcardId) {
        log.info("Received request to delete flashcard with id: {}", flashcardId);
        flashcardService.deleteFlashcard(flashcardId);
        return ResponseEntity.noContent().build();
    }
}