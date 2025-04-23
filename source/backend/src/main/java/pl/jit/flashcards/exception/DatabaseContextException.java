package pl.jit.flashcards.exception;

public class DatabaseContextException extends RuntimeException {
    public DatabaseContextException(String message, Throwable cause) {
        super(message, cause);
    }
} 