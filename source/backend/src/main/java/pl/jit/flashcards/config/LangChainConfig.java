package pl.jit.flashcards.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChainConfig {

    @Value("${ai.ollama.url:http://ollama:11434}")
    private String ollamaBaseUrl;

    @Value("${ai.ollama.model:gemma2:2b}")
    private String ollamaModel;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(ollamaModel)
                .temperature(0.2)  // Niższa temperatura = bardziej przewidywalne odpowiedzi
                .topP(0.95)        // Większy topP daje bardziej spójne i poprawne gramatycznie odpowiedzi
                .repeatPenalty(1.2) // Kara za powtarzanie, aby unikać dublowania treści
                .timeout(Duration.ofMinutes(5)) // 4 minuty na generowanie odpowiedzi
                .build();
    }
}