package pl.jit.flashcards.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import pl.jit.flashcards.exception.DatabaseContextException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
@Configuration
public class DatabaseConfig {
    private final DataSource dataSource;

    @Autowired
    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setUserContext(String userId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SET app.current_user_id = ?")) {
            stmt.setObject(1, userId);
            stmt.execute();
            log.debug("Set database user context for user: {}", userId);
        } catch (SQLException e) {
            log.error("Error setting database user context", e);
            throw new DatabaseContextException("Could not set database user context", e);
        }
    }

    public void clearUserContext() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("RESET app.current_user_id")) {
            stmt.execute();
            log.debug("Cleared database user context");
        } catch (SQLException e) {
            log.error("Error clearing database user context", e);
            throw new DatabaseContextException("Could not clear database user context", e);
        }
    }
} 