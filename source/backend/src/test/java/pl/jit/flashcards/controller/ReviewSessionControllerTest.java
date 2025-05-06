package pl.jit.flashcards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.jit.flashcards.data.api_model.FlashcardApiModel;
import pl.jit.flashcards.data.FlashcardSourceType;
import pl.jit.flashcards.data.request.LoginRequest;
import pl.jit.flashcards.data.request.RegisterRequest;
import pl.jit.flashcards.data.response.LoginResponse;
import pl.jit.flashcards.data.response.StartReviewSessionResponse;
import pl.jit.flashcards.exception.ResourceNotFoundException;
import pl.jit.flashcards.service.ReviewSessionService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewSessionService reviewSessionService;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register a new user
        String email = "reviewuser_" + UUID.randomUUID() + "@example.com";
        String password = "Password123!";
        RegisterRequest registerRequest = new RegisterRequest(email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        // Login to get the token
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseString, LoginResponse.class);
        authToken = "Bearer " + loginResponse.accessToken();
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsNotLoggedIn() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/review-sessions")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundWhenNoFlashcardsAvailableForReview() throws Exception {
        when(reviewSessionService.startReviewSession()).thenThrow(new ResourceNotFoundException("No flashcards available for review."));

        ResultActions result = mockMvc.perform(post("/api/review-sessions")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    void shouldStartReviewSessionSuccessfullyWhenFlashcardsExist() throws Exception {
        // given
        FlashcardApiModel flashcard1 = new FlashcardApiModel(
                UUID.randomUUID(),
                "Sample Front 1",
                "Sample Back 1",
                FlashcardSourceType.MANUAL,
                Instant.now()
        );
        FlashcardApiModel flashcard2 = new FlashcardApiModel(
                UUID.randomUUID(),
                "Sample Front 2",
                "Sample Back 2",
                FlashcardSourceType.AI_FULL,
                Instant.now()
        );
        List<FlashcardApiModel> mockFlashcards = List.of(flashcard1, flashcard2);
        StartReviewSessionResponse mockResponse = new StartReviewSessionResponse(mockFlashcards);

        when(reviewSessionService.startReviewSession()).thenReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(post("/api/review-sessions")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.flashcards").isArray())
                .andExpect(jsonPath("$.flashcards.length()").value(mockFlashcards.size()))
                .andExpect(jsonPath("$.flashcards[0].id").value(flashcard1.id().toString()))
                .andExpect(jsonPath("$.flashcards[0].frontContent").value(flashcard1.frontContent()))
                .andExpect(jsonPath("$.flashcards[0].backContent").value(flashcard1.backContent()))
                .andExpect(jsonPath("$.flashcards[0].sourceType").value(flashcard1.sourceType().getValue().toLowerCase()))
                .andExpect(jsonPath("$.flashcards[1].id").value(flashcard2.id().toString()))
                .andExpect(jsonPath("$.flashcards[1].sourceType").value(flashcard2.sourceType().getValue().toLowerCase()));
    }
}