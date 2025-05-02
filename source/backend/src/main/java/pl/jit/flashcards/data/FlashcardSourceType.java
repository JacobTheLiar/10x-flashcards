package pl.jit.flashcards.data;

import lombok.Getter;

@Getter
public enum FlashcardSourceType {
    AI_FULL("ai-full"),
    AI_EDITED("ai-edited"),
    MANUAL("manual");

    private final String value;

    FlashcardSourceType(String value) {
        this.value = value;
    }
}