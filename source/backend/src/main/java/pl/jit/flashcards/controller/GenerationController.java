package pl.jit.flashcards.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.jit.flashcards.data.request.GenerateFlashcardsRequest;
import pl.jit.flashcards.data.response.GenerateFlashcardsResponse;
import pl.jit.flashcards.service.GenerationService;

@RestController
@RequestMapping("/api/generations")
@RequiredArgsConstructor
@Slf4j
public class GenerationController {

    private final GenerationService generationService;

    @PostMapping
    public GenerateFlashcardsResponse generateFlashcards(@Valid @RequestBody GenerateFlashcardsRequest request) {
        log.info("Received flashcard generation request");
        return generationService.generateFlashcards(request);
    }
}