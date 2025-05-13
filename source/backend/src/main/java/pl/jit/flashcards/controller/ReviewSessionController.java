package pl.jit.flashcards.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import pl.jit.flashcards.data.response.StartReviewSessionResponse;
import pl.jit.flashcards.service.ReviewSessionService;

@RestController
@RequestMapping("/api/review-sessions")
@RequiredArgsConstructor
@Slf4j
public class ReviewSessionController {

    private final ReviewSessionService reviewSessionService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public StartReviewSessionResponse startReviewSession() {
        log.info("Received request to start a new review session.");
        return reviewSessionService.startReviewSession();
    }
}