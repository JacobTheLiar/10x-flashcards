package pl.jit.flashcards.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // given
        Map<String, String> registrationRequest = new HashMap<>();
        // Use a unique email for each test run to avoid conflicts between tests
        String uniqueEmail = "test_user_" + System.currentTimeMillis() + "@example.com";
        registrationRequest.put("email", uniqueEmail);
        registrationRequest.put("password", "Password123!");

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldReturnConflictWhenEmailIsAlreadyRegistered() throws Exception {
        // given: Register the first user
        Map<String, String> firstRegistrationRequest = new HashMap<>();
        String existingEmail = "duplicate_" + System.currentTimeMillis() + "@example.com";
        firstRegistrationRequest.put("email", existingEmail);
        firstRegistrationRequest.put("password", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRegistrationRequest)))
                .andExpect(status().isCreated()); // Ensure first registration is successful

        // and: Prepare a registration request with the same email
        Map<String, String> secondRegistrationRequest = new HashMap<>();
        secondRegistrationRequest.put("email", existingEmail); // Same email
        secondRegistrationRequest.put("password", "AnotherPassword456?");

        // when: Attempt to register the second user with the same email
        ResultActions result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRegistrationRequest)));

        // then: Expect a 409 Conflict status
        result.andExpect(status().isConflict());
    }

    @Test
    void shouldLoginUserSuccessfully() throws Exception {
        // given: Register a user first
        String userEmail = "login_" + System.currentTimeMillis() + "@example.com";
        String userPassword = "PasswordForLogin123!";
        Map<String, String> registrationRequest = new HashMap<>();
        registrationRequest.put("email", userEmail);
        registrationRequest.put("password", userPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated()); // Ensure registration is successful

        // and: Prepare login request
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", userEmail);
        loginRequest.put("password", userPassword);

        // when: Perform login request
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // then: Expect 200 OK and tokens in response
        result.andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginWithIncorrectPassword() throws Exception {
        // given: Register a user first
        String userEmail = "bad_login_" + System.currentTimeMillis() + "@example.com";
        String correctPassword = "PasswordForLogin123!";
        String incorrectPassword = "WrongPassword!";
        Map<String, String> registrationRequest = new HashMap<>();
        registrationRequest.put("email", userEmail);
        registrationRequest.put("password", correctPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated()); // Ensure registration is successful

        // and: Prepare login request with incorrect password
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", userEmail);
        loginRequest.put("password", incorrectPassword); // Use the wrong password

        // when: Perform login request
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // then: Expect 401 Unauthorized status
        result.andExpect(status().isUnauthorized()); // Expect 401
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        // given: Register and login a user to get tokens
        String userEmail = "refresh_" + System.currentTimeMillis() + "@example.com";
        String userPassword = "PasswordForRefresh123!";
        Map<String, String> registrationRequest = new HashMap<>();
        registrationRequest.put("email", userEmail);
        registrationRequest.put("password", userPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", userEmail);
        loginRequest.put("password", userPassword);

        ResultActions loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // Extract the refresh token from the login response using TypeReference for type safety
        String loginResponseContent = loginResult.andReturn().getResponse().getContentAsString();
        Map<String, Object> loginResponse = objectMapper.readValue(loginResponseContent, new TypeReference<>() {
        });
        String refreshToken = (String) loginResponse.get("refreshToken");

        // and: Prepare refresh token request
        Map<String, String> refreshTokenRequest = new HashMap<>();
        refreshTokenRequest.put("refreshToken", refreshToken);

        // when: Perform refresh token request
        ResultActions refreshResult = mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)));

        // then: Expect 200 OK and a new access token
        refreshResult.andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.accessToken").exists()) // New access token should exist
                .andExpect(jsonPath("$.expiresIn").exists()); // ExpiresIn should exist
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() throws Exception {
        // given: Prepare refresh token request with an invalid token
        Map<String, String> refreshTokenRequest = new HashMap<>();
        refreshTokenRequest.put("refreshToken", "invalid-or-expired-token");

        // when: Perform refresh token request
        ResultActions refreshResult = mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)));

        // then: Expect 401 Unauthorized status
        refreshResult.andExpect(status().isUnauthorized()); // Expect 401
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringWithMissingEmail() throws Exception {
        // given
        Map<String, String> registrationRequest = new HashMap<>();
        // registrationRequest.put("email", "test@example.com"); // Email is missing
        registrationRequest.put("password", "Password123!");

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)));

        // then
        result.andExpect(status().isBadRequest()); // Expect 400 Bad Request
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringWithInvalidEmailFormat() throws Exception {
        // given
        Map<String, String> registrationRequest = new HashMap<>();
        registrationRequest.put("email", "not-an-email"); // Invalid email format
        registrationRequest.put("password", "Password123!");

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)));

        // then
        result.andExpect(status().isBadRequest()); // Expect 400 Bad Request
    }

    @Test
    void shouldReturnBadRequestWhenRegisteringWithMissingPassword() throws Exception {
        // given
        Map<String, String> registrationRequest = new HashMap<>();
        String uniqueEmail = "validation_" + System.currentTimeMillis() + "@example.com";
        registrationRequest.put("email", uniqueEmail);
        // registrationRequest.put("password", "Password123!"); // Password is missing

        // when
        ResultActions result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)));

        // then
        result.andExpect(status().isBadRequest()); // Expect 400 Bad Request
    }
}