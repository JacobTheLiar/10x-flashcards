package pl.jit.flashcards.security;

import org.assertj.core.api.SoftAssertions; // Import dodany
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.jit.flashcards.entity.UserEntity;
import pl.jit.flashcards.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // given
        String email = "test@example.com";
        String passwordHash = "hashedPassword123";
        UUID userId = UUID.randomUUID();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);
        userEntity.setPasswordHash(passwordHash);
        userEntity.setCreatedAt(Instant.now());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // then
        SoftAssertions softly = new SoftAssertions(); // Użycie SoftAssertions
        softly.assertThat(userDetails).isNotNull();
        softly.assertThat(userDetails).isInstanceOf(UserDetailsImpl.class);
        softly.assertThat(userDetails.getUsername()).isEqualTo(email);
        softly.assertThat(userDetails.getPassword()).isEqualTo(passwordHash);
        softly.assertThat(((UserDetailsImpl) userDetails).getId()).isEqualTo(userId);

        // verify
        verify(userRepository).findByEmail(email);

        softly.assertAll();
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserDoesNotExist() {
        // given
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(nonExistentEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User Not Found with email: " + nonExistentEmail);

        // verify
        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void loadUserById_shouldReturnUserDetails_whenUserExists() {
        // given
        UUID userId = UUID.randomUUID();
        String email = "testById@example.com";
        String passwordHash = "hashedPassword456";

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);
        userEntity.setPasswordHash(passwordHash);
        userEntity.setCreatedAt(Instant.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // when
        UserDetails userDetails = userDetailsService.loadUserById(userId);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(userDetails).isNotNull();
        softly.assertThat(userDetails).isInstanceOf(UserDetailsImpl.class);
        softly.assertThat(userDetails.getUsername()).isEqualTo(email);
        softly.assertThat(userDetails.getPassword()).isEqualTo(passwordHash);
        softly.assertThat(((UserDetailsImpl) userDetails).getId()).isEqualTo(userId);

        // verify
        verify(userRepository).findById(userId);

        softly.assertAll();
    }

    @Test
    void loadUserById_shouldThrowUsernameNotFoundException_whenUserDoesNotExist() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> userDetailsService.loadUserById(nonExistentUserId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User Not Found with id: " + nonExistentUserId);

        // verify
        verify(userRepository).findById(nonExistentUserId);
    }
}
