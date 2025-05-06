package pl.jit.flashcards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import pl.jit.flashcards.data.FlashcardSourceType;
import pl.jit.flashcards.data.request.CreateFlashcardRequest;
import pl.jit.flashcards.data.request.LoginRequest;
import pl.jit.flashcards.data.request.RegisterRequest;
import pl.jit.flashcards.data.request.SaveFlashcardsRequest;
import pl.jit.flashcards.data.request.UpdateFlashcardRequest;
import pl.jit.flashcards.data.api_model.FlashcardApiModel;
import pl.jit.flashcards.data.response.GetAllFlashcardsResponse;
import pl.jit.flashcards.data.response.LoginResponse;
import pl.jit.flashcards.data.response.SaveFlashcardsResponse;
import pl.jit.flashcards.exception.ResourceNotFoundException;
import pl.jit.flashcards.service.FlashcardService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FlashcardService flashcardService;

    private String userAAuthToken;
    private String userBAuthToken;

    @BeforeEach
    void setUp() throws Exception {
        userAAuthToken = registerAndLoginUser("userA_");
        userBAuthToken = registerAndLoginUser("userB_");
    }

    private String registerAndLoginUser(String emailPrefix) throws Exception {
        String email = emailPrefix + UUID.randomUUID() + "@example.com";
        String password = "Password123!";
        RegisterRequest registerRequest = new RegisterRequest(email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseString, LoginResponse.class);
        return "Bearer " + loginResponse.accessToken();
    }

    @Test
    void shouldSaveFlashcardsSuccessfully() throws Exception {
        // given
        UUID generationId = UUID.randomUUID();
        CreateFlashcardRequest flashcardToSave1 = new CreateFlashcardRequest("Front 1", "Back 1", FlashcardSourceType.MANUAL);
        CreateFlashcardRequest flashcardToSave2 = new CreateFlashcardRequest("Front 2", "Back 2", FlashcardSourceType.AI_FULL);
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(generationId, List.of(flashcardToSave1, flashcardToSave2));

        FlashcardApiModel savedFlashcard1 = new FlashcardApiModel(UUID.randomUUID(), "Front 1", "Back 1", FlashcardSourceType.MANUAL, Instant.now());
        FlashcardApiModel savedFlashcard2 = new FlashcardApiModel(UUID.randomUUID(), "Front 2", "Back 2", FlashcardSourceType.AI_FULL, Instant.now());
        SaveFlashcardsResponse mockResponse = new SaveFlashcardsResponse(2, List.of(savedFlashcard1, savedFlashcard2));

        when(flashcardService.saveFlashcards(any(SaveFlashcardsRequest.class))).thenReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(post("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.savedCount").value(2))
                .andExpect(jsonPath("$.flashcards").isArray())
                .andExpect(jsonPath("$.flashcards.length()").value(2))
                .andExpect(jsonPath("$.flashcards[0].id").value(savedFlashcard1.id().toString()))
                .andExpect(jsonPath("$.flashcards[0].frontContent").value(savedFlashcard1.frontContent()))
                .andExpect(jsonPath("$.flashcards[0].sourceType").value(savedFlashcard1.sourceType().getValue())) 
                .andExpect(jsonPath("$.flashcards[1].id").value(savedFlashcard2.id().toString()));
    }

    @Test
    void shouldReturnBadRequestWhenSavingFlashcardsWithMissingGenerationId() throws Exception {
        // given
        CreateFlashcardRequest flashcardToSave = new CreateFlashcardRequest("Front", "Back", FlashcardSourceType.MANUAL);
        // generationId jest null
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(null, List.of(flashcardToSave));

        // when
        ResultActions result = mockMvc.perform(post("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSavingFlashcardsWithEmptyFlashcardList() throws Exception {
        // given
        UUID generationId = UUID.randomUUID();
        // Lista fiszek jest pusta
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(generationId, List.of());

        // when
        ResultActions result = mockMvc.perform(post("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSavingFlashcardsWithInvalidDataInList() throws Exception {
        // given
        UUID generationId = UUID.randomUUID();
        // Jedna z fiszek ma front content ustawiony na null, co jest niepoprawne
        CreateFlashcardRequest validFlashcard = new CreateFlashcardRequest("Valid Front", "Valid Back", FlashcardSourceType.MANUAL);
        CreateFlashcardRequest invalidFlashcard = new CreateFlashcardRequest(null, "Invalid Back", FlashcardSourceType.AI_EDITED);
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(generationId, List.of(validFlashcard, invalidFlashcard));

        // when
        ResultActions result = mockMvc.perform(post("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSavingFlashcardsWithMissingBackContentInList() throws Exception {
        // given
        UUID generationId = UUID.randomUUID();
        CreateFlashcardRequest invalidFlashcard = new CreateFlashcardRequest("Valid Front", null, FlashcardSourceType.MANUAL); // backContent jest null
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(generationId, List.of(invalidFlashcard));

        // when
        ResultActions result = mockMvc.perform(post("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSavingFlashcardsWithMissingSourceTypeInList() throws Exception {
        // given
        UUID generationId = UUID.randomUUID();
        CreateFlashcardRequest invalidFlashcard = new CreateFlashcardRequest("Valid Front", "Valid Back", null); // sourceType jest null
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(generationId, List.of(invalidFlashcard));

        // when
        ResultActions result = mockMvc.perform(post("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSavingFlashcardsWithTooLongFrontContentInList() throws Exception {
        // given
        UUID generationId = UUID.randomUUID();
        String tooLongFront = "a".repeat(501);
        CreateFlashcardRequest invalidFlashcard = new CreateFlashcardRequest(tooLongFront, "Valid Back", FlashcardSourceType.MANUAL);
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(generationId, List.of(invalidFlashcard));

        // when
        ResultActions result = mockMvc.perform(post("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSavingFlashcardsWithTooLongBackContentInList() throws Exception {
        // given
        UUID generationId = UUID.randomUUID();
        String tooLongBack = "a".repeat(201);
        CreateFlashcardRequest invalidFlashcard = new CreateFlashcardRequest("Valid Front", tooLongBack, FlashcardSourceType.MANUAL);
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(generationId, List.of(invalidFlashcard));

        // when
        ResultActions result = mockMvc.perform(post("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenSavingFlashcardsWithoutToken() throws Exception {
        // given
        UUID generationId = UUID.randomUUID();
        CreateFlashcardRequest flashcardToSave = new CreateFlashcardRequest("Front", "Back", FlashcardSourceType.MANUAL);
        SaveFlashcardsRequest request = new SaveFlashcardsRequest(generationId, List.of(flashcardToSave));

        // when
        ResultActions result = mockMvc.perform(post("/api/flashcards") // Brak nagłówka Authorization
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void shouldGetAllUserFlashcardsSuccessfully() throws Exception {
        // given
        FlashcardApiModel flashcard1 = new FlashcardApiModel(UUID.randomUUID(), "Front 1 GET", "Back 1 GET", FlashcardSourceType.MANUAL, Instant.now());
        FlashcardApiModel flashcard2 = new FlashcardApiModel(UUID.randomUUID(), "Front 2 GET", "Back 2 GET", FlashcardSourceType.AI_EDITED, Instant.now());
        List<FlashcardApiModel> mockFlashcards = List.of(flashcard1, flashcard2);
        GetAllFlashcardsResponse mockResponse = new GetAllFlashcardsResponse(mockFlashcards);

        when(flashcardService.getAllFlashcards()).thenReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.flashcards").isArray())
                .andExpect(jsonPath("$.flashcards.length()").value(2))
                .andExpect(jsonPath("$.flashcards[0].id").value(flashcard1.id().toString()))
                .andExpect(jsonPath("$.flashcards[0].frontContent").value(flashcard1.frontContent()))
                .andExpect(jsonPath("$.flashcards[0].sourceType").value(flashcard1.sourceType().getValue()));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoFlashcards() throws Exception {
        // given
        GetAllFlashcardsResponse mockResponse = new GetAllFlashcardsResponse(List.of());

        when(flashcardService.getAllFlashcards()).thenReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/flashcards")
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.flashcards").isArray())
                .andExpect(jsonPath("$.flashcards.length()").value(0));
    }

    @Test
    void shouldReturnUnauthorizedWhenGettingAllFlashcardsWithoutToken() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/flashcards")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateFlashcardSuccessfully() throws Exception {
        // given
        UUID flashcardId = UUID.randomUUID();
        UpdateFlashcardRequest updateRequest = new UpdateFlashcardRequest("Updated Front Content", "Updated Back Content");
        FlashcardApiModel updatedFlashcardResponse = new FlashcardApiModel(
                flashcardId,
                updateRequest.frontContent(),
                updateRequest.backContent(),
                FlashcardSourceType.AI_EDITED, // Zakładamy, że serwis odpowiednio ustawi ten typ
                Instant.now()
        );

        when(flashcardService.updateFlashcard(eq(flashcardId), any(UpdateFlashcardRequest.class)))
                .thenReturn(updatedFlashcardResponse);

        // when
        ResultActions result = mockMvc.perform(put("/api/flashcards/{flashcardId}", flashcardId)
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(flashcardId.toString()))
                .andExpect(jsonPath("$.frontContent").value(updateRequest.frontContent()))
                .andExpect(jsonPath("$.backContent").value(updateRequest.backContent()))
                .andExpect(jsonPath("$.sourceType").value(FlashcardSourceType.AI_EDITED.getValue()));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingFlashcard() throws Exception {
        // given
        UUID nonExistingFlashcardId = UUID.randomUUID();
        UpdateFlashcardRequest updateRequest = new UpdateFlashcardRequest("Any Front", "Any Back");

        when(flashcardService.updateFlashcard(eq(nonExistingFlashcardId), any(UpdateFlashcardRequest.class)))
                .thenThrow(new ResourceNotFoundException("Flashcard not found: " + nonExistingFlashcardId));

        // when
        ResultActions result = mockMvc.perform(put("/api/flashcards/{flashcardId}", nonExistingFlashcardId)
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingFlashcardWithInvalidData() throws Exception {
        // given
        UUID flashcardId = UUID.randomUUID();
        UpdateFlashcardRequest invalidRequest = new UpdateFlashcardRequest("", "Updated Back"); // Pusty frontContent

        // when
        ResultActions result = mockMvc.perform(put("/api/flashcards/{flashcardId}", flashcardId)
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingFlashcardWithEmptyBackContent() throws Exception {
        // given
        UUID flashcardId = UUID.randomUUID();
        UpdateFlashcardRequest invalidRequest = new UpdateFlashcardRequest("Valid Front", ""); // Pusty backContent

        // when
        ResultActions result = mockMvc.perform(put("/api/flashcards/{flashcardId}", flashcardId)
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingFlashcardWithTooLongFrontContent() throws Exception {
        // given
        UUID flashcardId = UUID.randomUUID();
        String tooLongFront = "a".repeat(501);
        UpdateFlashcardRequest invalidRequest = new UpdateFlashcardRequest(tooLongFront, "Valid Back");

        // when
        ResultActions result = mockMvc.perform(put("/api/flashcards/{flashcardId}", flashcardId)
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingFlashcardWithTooLongBackContent() throws Exception {
        // given
        UUID flashcardId = UUID.randomUUID();
        String tooLongBack = "a".repeat(201);
        UpdateFlashcardRequest invalidRequest = new UpdateFlashcardRequest("Valid Front", tooLongBack);

        // when
        ResultActions result = mockMvc.perform(put("/api/flashcards/{flashcardId}", flashcardId)
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenUpdatingFlashcardWithoutToken() throws Exception {
        // given
        UUID flashcardId = UUID.randomUUID();
        UpdateFlashcardRequest updateRequest = new UpdateFlashcardRequest("Some Front", "Some Back");

        // when
        ResultActions result = mockMvc.perform(put("/api/flashcards/{flashcardId}", flashcardId)
                // Brak nagłówka Authorization
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDeleteFlashcardSuccessfully() throws Exception {
        // given
        UUID flashcardId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(delete("/api/flashcards/{flashcardId}", flashcardId)
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken));

        // then
        result.andExpect(status().isNoContent());
        // Weryfikujemy, czy metoda serwisu została wywołana z poprawnym ID
        verify(flashcardService).deleteFlashcard(flashcardId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingFlashcard() throws Exception {
        // given
        UUID nonExistingFlashcardId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Flashcard not found with id: " + nonExistingFlashcardId))
                .when(flashcardService).deleteFlashcard(nonExistingFlashcardId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/flashcards/{flashcardId}", nonExistingFlashcardId)
                .header(HttpHeaders.AUTHORIZATION, userAAuthToken));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWhenDeletingFlashcardWithoutToken() throws Exception {
        // given
        UUID flashcardId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(delete("/api/flashcards/{flashcardId}", flashcardId));
        // Bez nagłówka Authorization

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenUpdatingFlashcardNotOwnedByUser() throws Exception {
        // given
        UUID flashcardIdOwnedByUserA = UUID.randomUUID();
        UpdateFlashcardRequest updateRequest = new UpdateFlashcardRequest("Attempted Update Front", "Attempted Update Back");

        doThrow(new AccessDeniedException("User does not own this flashcard"))
                .when(flashcardService).updateFlashcard(eq(flashcardIdOwnedByUserA), any(UpdateFlashcardRequest.class));

        ResultActions result = mockMvc.perform(put("/api/flashcards/{flashcardId}", flashcardIdOwnedByUserA)
                .header(HttpHeaders.AUTHORIZATION, userBAuthToken) // Token UserB
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        result.andExpect(status().isForbidden()); // Oczekujemy 403 Forbidden
    }

    @Test
    void shouldReturnForbiddenWhenDeletingFlashcardNotOwnedByUser() throws Exception {
        // given
        UUID flashcardIdOwnedByUserA = UUID.randomUUID();

        doThrow(new AccessDeniedException("User does not own this flashcard"))
                .when(flashcardService).deleteFlashcard(flashcardIdOwnedByUserA);

        ResultActions result = mockMvc.perform(delete("/api/flashcards/{flashcardId}", flashcardIdOwnedByUserA)
                .header(HttpHeaders.AUTHORIZATION, userBAuthToken));

        // then
        result.andExpect(status().isForbidden());
    }
}