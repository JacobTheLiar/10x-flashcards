package pl.jit.flashcards.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import pl.jit.flashcards.data.api_model.FlashcardSuggestionApiModel;
import pl.jit.flashcards.service.AiIntegrationService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Primary
@Slf4j
public class OllamaAiIntegrationService implements AiIntegrationService {

    private final ChatLanguageModel chatLanguageModel;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${ai.ollama.model:gemma2:2b}")
    private String modelName;

    private final String systemPrompt;

    public OllamaAiIntegrationService(ChatLanguageModel chatLanguageModel, ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.chatLanguageModel = chatLanguageModel;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.systemPrompt = loadPromptFromFile();
    }

    private String loadPromptFromFile() {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompt.md");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            String prompt = org.springframework.util.FileCopyUtils.copyToString(reader);
            log.debug("Loaded prompt from file, length: {}", prompt.length());
            return prompt;
        } catch (IOException e) {
            log.error("Failed to load prompt from file", e);
            // Fallback to default prompt
            return """
                    Jesteś ekspertem w tworzeniu fiszek edukacyjnych dla studentów. Tworzysz fiszki na podstawie tekstu.
                    Fiszka składa się z dwóch części:
                    1. "frontContent" - pytanie, pojęcie lub termin (maksymalnie 500 znaków)
                    2. "backContent" - odpowiedź, definicja lub wyjaśnienie (maksymalnie 200 znaków)
                    
                    Zasady tworzenia fiszek:
                    - Twórz fiszki na podstawie kluczowych pojęć i informacji z tekstu
                    - Pytania powinny być jasne i konkretne
                    - Odpowiedzi powinny być zwięzłe i precyzyjne
                    - Nie twórz fiszek na temat informacji, których nie ma w tekście
                    - Fiszki powinny obejmować najważniejsze informacje z tekstu
                    
                    Zwróć wyniki WYŁĄCZNIE w formacie JSON jako tablicę obiektów zawierających pola "frontContent" i "backContent".
                    Nie używaj żadnych znaczników markdown.
                    """;
        }
    }

    @Override
    public List<FlashcardSuggestionApiModel> generateSuggestions(String sourceText) {
        log.info("Generating flashcard suggestions using {} for text starting with: '{}'...",
                modelName, sourceText.substring(0, Math.min(sourceText.length(), 50)));

        try {
            SystemMessage systemMessage = SystemMessage.from(systemPrompt);
            UserMessage userMessage = UserMessage.from("Wygeneruj fiszki na podstawie tekstu: " + sourceText);

            log.debug("Sending request to LLM with system prompt length: {} and text length: {}",
                    systemPrompt.length(), sourceText.length());

            Response<AiMessage> response = chatLanguageModel.generate(systemMessage, userMessage);
            String content = response.content().text();
            log.debug("AI response received, content length: {}", content.length());
            if (log.isTraceEnabled()) {
                log.trace("AI raw response: {}", content);
            }

            return parseJsonResponse(content);
        } catch (Exception e) {
            log.error("Error generating suggestions with LangChain4j", e);
            return List.of(
                    new FlashcardSuggestionApiModel(
                            "Błąd generowania fiszek",
                            "Spróbuj ponownie później"
                    )
            );
        }
    }

    private List<FlashcardSuggestionApiModel> parseJsonResponse(String jsonResponse) {
        try {
            String cleanedJson = cleanJsonResponse(jsonResponse);
            log.debug("Cleaned JSON: {}", cleanedJson);

            return objectMapper.readValue(cleanedJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response: {}", jsonResponse, e);

            try {
                String fixedJson = attemptToFixJson(jsonResponse);
                log.debug("Trying to parse fixed JSON: {}", fixedJson);
                return objectMapper.readValue(fixedJson, new TypeReference<>() {
                });
            } catch (JsonProcessingException e2) {
                log.error("Failed to parse fixed JSON too", e2);
                return List.of(
                    new FlashcardSuggestionApiModel(
                            "Błąd przetwarzania odpowiedzi AI",
                            "Format odpowiedzi jest nieprawidłowy"
                    )
                );
            }
        }
    }

    private String attemptToFixJson(String response) {
        String result = response
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int startIndex = result.indexOf('[');
        int endIndex = result.lastIndexOf(']');

        if (startIndex >= 0 && endIndex > startIndex) {
            String jsonArray = result.substring(startIndex, endIndex + 1);
            String originalArray = jsonArray;
            jsonArray = jsonArray.replaceAll(",\\s*]", "]");

            if (!originalArray.equals(jsonArray)) {
                log.debug("Fixed trailing comma in JSON array");
            }

            // Usuń niedozwolone znaki kontrolne
            jsonArray = jsonArray.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

            // Napraw niepoprawne ucieczki w łańcuchach znaków
            jsonArray = jsonArray.replaceAll("(?<!\\\\)\\\\(?!([\"\\\\/bfnrt]|u[0-9a-fA-F]{4}))", "\\\\\\\\");

            // Napraw podwójne cudzysłowy w łańcuchach znaków
            jsonArray = jsonArray.replaceAll("(?<!\\\\)\"(?=.*\"\\s*:)", "\\\\\"");

            return jsonArray;
        }
        log.warn("Nie znaleziono poprawnej tablicy JSON w przypadku naprawy: {}", result);
        return result;
    }

    private String cleanJsonResponse(String response) {
        response = response
                .replace("```json", "")
                .replace("```", "")
                .trim();
        int startIndex = response.indexOf('[');
        int endIndex = response.lastIndexOf(']');

        if (startIndex >= 0 && endIndex > startIndex) {
            String jsonArray = response.substring(startIndex, endIndex + 1);
            jsonArray = jsonArray.replaceAll(",\\s*]", "]");
            return jsonArray;
        }

        log.warn("Nie znaleziono poprawnej tablicy JSON w odpowiedzi: {}", response);
        return response;
    }
}