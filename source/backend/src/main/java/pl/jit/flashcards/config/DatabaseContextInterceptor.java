package pl.jit.flashcards.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class DatabaseContextInterceptor implements HandlerInterceptor {

    private final DatabaseConfig databaseConfig;

    public DatabaseContextInterceptor(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String userIdStr = authentication.getName();
            try {
                log.info("Setting RLS user context for user ID: {}", userIdStr);
                databaseConfig.setUserContext(userIdStr);
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse UUID from token subject: {}", userIdStr, e);
                return false;
            } catch (Exception e) {
                log.error("Failed to set RLS user context for user ID: {}. Error: {}", userIdStr, e.getMessage(), e);
                return false;
            }
        } else {
            log.debug("Skipping RLS user context setting for anonymous or unauthenticated user.");
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            log.debug("Clearing RLS user context.");
            databaseConfig.clearUserContext();
        }
    }
}