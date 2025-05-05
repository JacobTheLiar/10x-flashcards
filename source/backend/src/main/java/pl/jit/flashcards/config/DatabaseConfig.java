package pl.jit.flashcards.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import pl.jit.flashcards.exception.DatabaseContextException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {
    private final DataSource dataSource;

    public void setUserContext(String userId) {
        // 1. Próba ustawienia kontekstu
        try (Connection conn = dataSource.getConnection();
             PreparedStatement setStmt = conn.prepareStatement("SELECT set_config('app.current_user_id', ?, false);")) {
            setStmt.setString(1, userId);
            setStmt.execute();
            log.debug("Attempted to set database user context for user: {}", userId);
        } catch (SQLException e) {
            log.error("SQL Error during initial set_config for user ID: {}", userId, e);
            throw new DatabaseContextException("Could not set database user context initially", e);
        }

        // 2. Weryfikacja ustawienia kontekstu
        String retrievedUserId = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT current_setting('app.current_user_id', true);")) {

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    retrievedUserId = rs.getString(1);
                } else {
                    log.warn("current_setting('app.current_user_id', true) returned no rows.");
                }
            }
            log.debug("Retrieved current_setting('app.current_user_id') after set attempt: {}", retrievedUserId);

        } catch (SQLException e) {
            log.error("SQL Error while verifying user context setting for user ID: {}. Error: {}", userId, e.getMessage(), e);
            // Rzucamy wyjątek, bo nie można zweryfikować, czy kontekst jest poprawny
            throw new DatabaseContextException("Could not verify database user context setting due to SQL error", e);
        }

        // 3. Porównanie i obsługa niezgodności
        if (retrievedUserId == null || !retrievedUserId.equals(userId)) {
            log.error("CRITICAL: RLS context verification failed! Expected '{}' but got '{}'. RLS may not be effective.", userId, retrievedUserId);
            throw new DatabaseContextException("Failed to verify database user context setting. Expected: " + userId + ", Got: " + retrievedUserId);
        } else {
            log.debug("Successfully verified database user context set to: {}", retrievedUserId);
        }
    }

    public void clearUserContext() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("RESET app.current_user_id;")) {
            stmt.execute();
            log.debug("Cleared database user context");
        } catch (SQLException e) {
            log.error("Error clearing database user context", e);
            throw new DatabaseContextException("Could not clear database user context", e);
        }
    }
}