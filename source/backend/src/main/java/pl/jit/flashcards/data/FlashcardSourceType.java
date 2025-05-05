package pl.jit.flashcards.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum FlashcardSourceType {
    AI_FULL("ai-full"),
    AI_EDITED("ai-edited"),
    MANUAL("manual");

    private final String value;

    FlashcardSourceType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static FlashcardSourceType fromString(String text) {
        return Arrays.stream(FlashcardSourceType.values())
                .filter(type -> type.value.equalsIgnoreCase(text)) // Porównujemy z polem 'value'
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown sourceType: " + text)); // Rzucamy wyjątek, jeśli nie znaleziono
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}