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
import pl.jit.flashcards.data.request.GenerateFlashcardsRequest;
import pl.jit.flashcards.data.request.LoginRequest;
import pl.jit.flashcards.data.request.RegisterRequest;
import pl.jit.flashcards.data.response.GenerateFlashcardsResponse;
import pl.jit.flashcards.data.api_model.FlashcardSuggestionApiModel;
import pl.jit.flashcards.data.response.LoginResponse;
import pl.jit.flashcards.service.GenerationService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GenerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenerationService generationService;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register a new user
        String email = "testuser_" + UUID.randomUUID() + "@example.com";
        String password = "Password123!";
        RegisterRequest registerRequest = new RegisterRequest(email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get the token
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseString, LoginResponse.class);
        authToken = "Bearer " + loginResponse.accessToken();
    }

    @Test
    void shouldGenerateFlashcardsSuccessfully() throws Exception {
        // given
        GenerateFlashcardsRequest request = new GenerateFlashcardsRequest("This is a sample text for flashcard generation.");
        GenerateFlashcardsResponse mockResponse = new GenerateFlashcardsResponse(
                UUID.randomUUID(),
                Instant.now(),
                1500L,
                List.of(new FlashcardSuggestionApiModel("Front", "Back"))
        );

        when(generationService.generateFlashcards(any(GenerateFlashcardsRequest.class))).thenReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(post("/api/generations")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.generationId").value(mockResponse.generationId().toString()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.generationTimeMs").value(mockResponse.generationTimeMs()))
                .andExpect(jsonPath("$.suggestedFlashcards[0].frontContent").value("Front"))
                .andExpect(jsonPath("$.suggestedFlashcards[0].backContent").value("Back"));
    }

    @Test
    void shouldReturnBadRequestWhenSourceTextIsTooLong() throws Exception {
        // given
        String longText = "a".repeat(10001);
        GenerateFlashcardsRequest request = new GenerateFlashcardsRequest(longText);

        // when
        ResultActions result = mockMvc.perform(post("/api/generations")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSourceTextIsEmpty() throws Exception {
        // given
        GenerateFlashcardsRequest request = new GenerateFlashcardsRequest("");

        // when
        ResultActions result = mockMvc.perform(post("/api/generations")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSourceTextIsMissing() throws Exception {
        // given
        String jsonRequest = "{}";

        // when
        ResultActions result = mockMvc.perform(post("/api/generations")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));
        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorWhenGenerationFails() throws Exception {
        // given
        GenerateFlashcardsRequest request = new GenerateFlashcardsRequest("Valid text.");
        when(generationService.generateFlashcards(any(GenerateFlashcardsRequest.class)))
                .thenThrow(new RuntimeException("Simulated generation error"));

        // when
        ResultActions result = mockMvc.perform(post("/api/generations")
                .header(HttpHeaders.AUTHORIZATION, authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        // then
        result.andExpect(status().isInternalServerError());
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsNotLoggedIn() throws Exception {
        // given
        GenerateFlashcardsRequest request = new GenerateFlashcardsRequest("Some text");

        // when
        ResultActions result = mockMvc.perform(post("/api/generations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isUnauthorized());
    }
}