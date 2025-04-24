# REST API Plan

## 1. Resources

- **Users** - Corresponds to `users` table, represents system users
- **Generations** - Corresponds to `generations` table, represents flashcard generation processes
- **Flashcards** - Corresponds to `flashcards` table, represents individual flashcards
- **Review Sessions** - Virtual resource for managing review functionality
- **Authentication** - Handles user registration, login, and token management

## 2. Endpoints

### 2.1. Authentication

#### Register a new user
- Method: POST
- URL Path: `/api/auth/register`
- Description: Creates a new user account
- Request Payload:
  ```json
  {
    "email": "user@example.com",
    "password": "securePassword123"
  }
  ```
- Response Payload:
  ```json
  {
    "id": "uuid",
    "email": "user@example.com",
    "createdAt": "2023-04-20T14:30:00Z"
  }
  ```
- Success Codes:
  - 201 Created - Account successfully created
- Error Codes:
  - 400 Bad Request - Invalid input data (e.g., email format, password requirements)
  - 409 Conflict - Email already in use

#### User login
- Method: POST
- URL Path: `/api/auth/login`
- Description: Authenticates a user and provides access tokens
- Request Payload:
  ```json
  {
    "email": "user@example.com",
    "password": "securePassword123"
  }
  ```
- Response Payload:
  ```json
  {
    "accessToken": "jwt-token",
    "refreshToken": "refresh-token",
    "expiresIn": 3600
  }
  ```
- Success Codes:
  - 200 OK - Successfully authenticated
- Error Codes:
  - 400 Bad Request - Invalid credentials
  - 401 Unauthorized - Invalid email or password

#### Refresh token
- Method: POST
- URL Path: `/api/auth/refresh-token`
- Description: Provides a new access token using the refresh token
- Request Payload:
  ```json
  {
    "refreshToken": "refresh-token"
  }
  ```
- Response Payload:
  ```json
  {
    "accessToken": "new-jwt-token",
    "expiresIn": 3600
  }
  ```
- Success Codes:
  - 200 OK - Token successfully refreshed
- Error Codes:
  - 400 Bad Request - Invalid refresh token
  - 401 Unauthorized - Expired refresh token

### 2.2. Generations

#### Generate flashcards
- Method: POST
- URL Path: `/api/generations`
- Description: Generates flashcard suggestions from input text
- Request Payload:
  ```json
  {
    "sourceText": "Text content (up to 10,000 characters)"
  }
  ```
- Response Payload:
  ```json
  {
    "generationId": "uuid",
    "createdAt": "2023-04-20T14:30:00Z",
    "generationTimeMs": 1500,
    "suggestedFlashcards": [
      {
        "frontContent": "Question content",
        "backContent": "Answer content"
      },
      // More flashcards
    ]
  }
  ```
- Success Codes:
  - 200 OK - Flashcards successfully generated
- Error Codes:
  - 400 Bad Request - Invalid input (e.g., text too long)
  - 500 Internal Server Error - Generation failed

### 2.3. Flashcards

#### Save accepted flashcards
- Method: POST
- URL Path: `/api/flashcards`
- Description: Saves a list of accepted flashcards from generation
- Request Payload:
  ```json
  {
    "generationId": "uuid",
    "flashcards": [
      {
        "frontContent": "Question content",
        "backContent": "Answer content",
        "sourceType": "ai-full" // One of: ai-full, ai-edited, manual
      },
      // More flashcards
    ]
  }
  ```
- Response Payload:
  ```json
  {
    "savedCount": 5,
    "flashcards": [
      {
        "id": "uuid",
        "frontContent": "Question content",
        "backContent": "Answer content",
        "sourceType": "ai-full",
        "lastModifiedAt": "2023-04-20T14:35:00Z"
      },
      // More flashcards
    ]
  }
  ```
- Success Codes:
  - 201 Created - Flashcards saved successfully
- Error Codes:
  - 400 Bad Request - Invalid input data
  - 401 Unauthorized - User not authenticated
  - 403 Forbidden - User doesn't own this generation

#### Get all user's flashcards
- Method: GET
- URL Path: `/api/flashcards`
- Description: Retrieves all user's saved flashcards
- Response Payload:
  ```json
  {
    "flashcards": [
      {
        "id": "uuid",
        "frontContent": "Question content",
        "backContent": "Answer content",
        "sourceType": "ai-full|ai-edited|manual",
        "lastModifiedAt": "2023-04-20T14:35:00Z"
      },
      // More flashcards
    ]
  }
  ```
- Success Codes:
  - 200 OK - List retrieved successfully
- Error Codes:
  - 401 Unauthorized - User not authenticated

#### Update a flashcard
- Method: PUT
- URL Path: `/api/flashcards/{flashcardId}`
- Description: Updates an existing flashcard
- Request Payload:
  ```json
  {
    "frontContent": "Updated question content",
    "backContent": "Updated answer content"
  }
  ```
- Response Payload:
  ```json
  {
    "id": "uuid",
    "frontContent": "Updated question content",
    "backContent": "Updated answer content",
    "sourceType": "ai-edited",
    "lastModifiedAt": "2023-04-20T15:00:00Z"
  }
  ```
- Success Codes:
  - 200 OK - Flashcard updated successfully
- Error Codes:
  - 400 Bad Request - Invalid input data
  - 401 Unauthorized - User not authenticated
  - 403 Forbidden - User doesn't own this flashcard
  - 404 Not Found - Flashcard not found

#### Delete a flashcard
- Method: DELETE
- URL Path: `/api/flashcards/{flashcardId}`
- Description: Deletes a flashcard
- Response Payload: None
- Success Codes:
  - 204 No Content - Flashcard deleted successfully
- Error Codes:
  - 401 Unauthorized - User not authenticated
  - 403 Forbidden - User doesn't own this flashcard
  - 404 Not Found - Flashcard not found

### 2.4. Review Sessions

#### Start a review session
- Method: POST
- URL Path: `/api/review-sessions`
- Description: Creates a new review session with randomly selected flashcards
- Request Payload: None (system uses default range of 5-15 flashcards)
- Response Payload:
  ```json
  {
    "flashcards": [
      {
        "id": "uuid",
        "frontContent": "Question content",
        "backContent": "Answer content"
      },
      // More flashcards
    ]
  }
  ```
- Success Codes:
  - 200 OK - Review session created successfully
- Error Codes:
  - 401 Unauthorized - User not authenticated
  - 404 Not Found - No flashcards available for review

## 3. Authentication and Authorization

### JWT-Based Authentication

The API will use JSON Web Tokens (JWT) for authentication with the following implementation details:

1. **Token Structure**:
   - Access Token: Short-lived JWT containing user claims
   - Refresh Token: Longer-lived token to obtain new access tokens

2. **Security Measures**:
   - Access tokens expire after 1 hour
   - Refresh tokens expire after 7 days
   - Tokens are signed with a secure algorithm (HMAC SHA-256)
   - CSRF protection for sensitive operations
   - HTTPS-only communication

3. **Authorization Flow**:
   - User registers or logs in to receive an access token and refresh token
   - All API requests include the access token in the Authorization header
   - When the access token expires, the refresh token is used to obtain a new one
   - Tokens can be invalidated on logout or security breach

4. **Implementation with Spring Security**:
   - JWT filter to validate tokens
   - Role-based access control
   - Method-level security annotations

## 4. Validation and Business Logic

### 4.1. Validation Rules

#### Users
- Email must be a valid email format
- Email must be unique in the system
- Password must meet security requirements (e.g., minimum 8 characters, containing letters, numbers, and special characters)

#### Flashcards
- Front content must not exceed 500 characters
- Back content must not exceed 200 characters
- Source type must be one of: 'ai-full', 'ai-edited', 'manual'

#### Generations
- Source text must not exceed 10,000 characters
- Source text must not be empty

#### Review Sessions
- Default range of 5-15 randomly selected flashcards

### 4.2. Business Logic Implementation

#### Flashcard Generation
- The generation process records metadata but does not automatically save flashcards
- Frontend is responsible for managing the approval process
- Flashcards are only saved when explicitly submitted via the save endpoint

#### Flashcard Management
- Users can only view, edit, and delete their own flashcards
- Updating a flashcard originating from AI changes its source type to 'ai-edited'
- Modification timestamps are updated on each edit
- Frontend handles the decision-making process for accepting or rejecting flashcards

#### Review System
- Flashcards are randomly selected within the default range
- Frontend is responsible for managing navigation between flashcards
- Backend simply provides the randomly selected list of flashcards

#### Row Level Security
- The application sets a database session context for each authenticated request
- All database queries include user context to enforce data isolation
- API maintains double security with both application-level filters and database-level RLS

#### Error Tracking
- Generation errors are recorded with specific codes and messages
- Errors do not prevent the operation of the rest of the system