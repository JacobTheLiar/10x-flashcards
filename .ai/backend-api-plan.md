# Plan Implementacji Serwisów i Kontrolerów - 10x Flashcards

Ten dokument łączy strukturę wymaganą do stworzenia planu implementacji z istniejącą definicją REST API. Jest to punkt wyjściowy do szczegółowego rozpisania implementacji serwisów i kontrolerów dla aplikacji 10x Flashcards.

**Uwaga:** Pełna implementacja wymaga uwzględnienia informacji z plików `db-plan.md`, `prd.md`, `backend-rule.md` oraz `helper.md`, zgodnie z wytycznymi z oryginalnego pliku `api-implementation-plan.md`. Na tym etapie nie implementujemy Spring Security.

## 1. Przegląd architektury serwisów i kontrolerów

*   **(Do uzupełnienia)** Diagram zależności między komponentami.
*   **Opis odpowiedzialności komponentów:**
    *   **Kontrolery:** Odpowiedzialne za obsługę żądań HTTP, walidację danych wejściowych, wywoływanie odpowiednich metod serwisów i zwracanie odpowiedzi HTTP. Mapują ścieżki URL na logikę biznesową.
    *   **Serwisy:** Zawierają logikę biznesową aplikacji. Współpracują z repozytoriami w celu uzyskania dostępu do danych, przetwarzają dane i realizują operacje biznesowe. Odpowiadają za zarządzanie transakcjami i obsługę wyjątków biznesowych.
    *   **Repozytoria:** Interfejsy Spring Data JPA do interakcji z bazą danych.
    *   **Mappery (MapStruct):** Odpowiedzialne za konwersję między encjami JPA a obiektami DTO (Data Transfer Objects).
    *   **AI Service (Stub):** Wydmuszka serwisu do integracji z modelem AI (Ollama) na potrzeby generowania fiszek.

## 2. Serwisy - szczegółowa specyfikacja

*(Na tym etapie definiujemy główne serwisy i ich ogólną odpowiedzialność, bazując na zasobach API. Szczegółowe metody publiczne i prywatne, obsługa wyjątków oraz transakcyjność zostaną zdefiniowane w kolejnym kroku, zgodnie z `api-implementation-plan.md`)*

*   **`AuthService`**:
    *   Opis: Zarządzanie procesami rejestracji, logowania i odświeżania tokenów użytkowników.
    *   Zależności: `UserRepository`, `PasswordEncoder`, `JwtTokenProvider` (do implementacji później lub jako stub na razie).
*   **`UserService`**:
    *   Opis: Zarządzanie danymi użytkowników (potencjalnie potrzebny, jeśli pojawią się operacje CRUD na użytkownikach poza rejestracją).
    *   Zależności: `UserRepository`.
*   **`GenerationService`**:
    *   Opis: Obsługa procesu generowania sugestii fiszek na podstawie tekstu źródłowego. Komunikacja z serwisem AI (stub).
    *   Zależności: `GenerationRepository`, `AiService` (stub), `FlashcardMapper`.
*   **`FlashcardService`**:
    *   Opis: Zarządzanie fiszkami użytkownika - zapisywanie, pobieranie, aktualizacja, usuwanie. Implementacja logiki biznesowej związanej z fiszkami (np. zmiana `sourceType`).
    *   Zależności: `FlashcardRepository`, `GenerationRepository`, `UserRepository` (lub mechanizm RLS), `FlashcardMapper`.
*   **`ReviewSessionService`**:
    *   Opis: Tworzenie sesji powtórkowych poprzez losowe wybieranie fiszek użytkownika do przeglądu.
    *   Zależności: `FlashcardRepository`, `UserRepository` (lub mechanizm RLS), `FlashcardMapper`.
*   **`AiService` (Stub/Interface)**:
    *   Opis: Prosty interfejs i implementacja wydmuszki/mocka do symulowania generowania fiszek przez AI.
    *   Zależności: Brak (na tym etapie).

## 3. Kontrolery - szczegółowa specyfikacja

*(Poniżej znajduje się lista kontrolerów i ich metod, zgodnie z definicją w `api-plan.md`. Walidacja danych wejściowych i szczegółowe mapowanie na metody serwisów zostaną dodane w kolejnym kroku)*

### 3.1. `AuthController`

*   Endpoint bazowy: `/api/auth`
*   Zależności: `AuthService`

*   **Register a new user**
    *   Metoda: `POST`
    *   Ścieżka: `/register`
    *   Opis: Tworzy nowe konto użytkownika.
    *   Request Payload: `UserRegistrationDto` (`email`, `password`)
    *   Response Payload: `UserDto` (`id`, `email`, `createdAt`)
    *   Kody sukcesu: 201 Created
    *   Kody błędów: 400 Bad Request, 409 Conflict
*   **User login**
    *   Metoda: `POST`
    *   Ścieżka: `/login`
    *   Opis: Uwierzytelnia użytkownika i dostarcza tokeny dostępu.
    *   Request Payload: `LoginRequestDto` (`email`, `password`)
    *   Response Payload: `JwtTokenDto` (`accessToken`, `refreshToken`, `expiresIn`)
    *   Kody sukcesu: 200 OK
    *   Kody błędów: 400 Bad Request, 401 Unauthorized
*   **Refresh token**
    *   Metoda: `POST`
    *   Ścieżka: `/refresh-token`
    *   Opis: Dostarcza nowy token dostępu przy użyciu tokena odświeżania.
    *   Request Payload: `RefreshTokenRequestDto` (`refreshToken`)
    *   Response Payload: `AccessTokenDto` (`accessToken`, `expiresIn`)
    *   Kody sukcesu: 200 OK
    *   Kody błędów: 400 Bad Request, 401 Unauthorized

### 3.2. `GenerationController`

*   Endpoint bazowy: `/api/generations`
*   Zależności: `GenerationService`

*   **Generate flashcards**
    *   Metoda: `POST`
    *   Ścieżka: `/`
    *   Opis: Generuje sugestie fiszek na podstawie tekstu wejściowego.
    *   Request Payload: `GenerationRequestDto` (`sourceText`)
    *   Response Payload: `GenerationResponseDto` (`generationId`, `createdAt`, `generationTimeMs`, `suggestedFlashcards`: List<`FlashcardSuggestionDto`>)
    *   Kody sukcesu: 200 OK
    *   Kody błędów: 400 Bad Request, 500 Internal Server Error

### 3.3. `FlashcardController`

*   Endpoint bazowy: `/api/flashcards`
*   Zależności: `FlashcardService`

*   **Save accepted flashcards**
    *   Metoda: `POST`
    *   Ścieżka: `/`
    *   Opis: Zapisuje listę zaakceptowanych fiszek z procesu generowania.
    *   Request Payload: `SaveFlashcardsRequestDto` (`generationId`, `flashcards`: List<`FlashcardCreateDto`>)
    *   Response Payload: `SaveFlashcardsResponseDto` (`savedCount`, `flashcards`: List<`FlashcardDto`>)
    *   Kody sukcesu: 201 Created
    *   Kody błędów: 400 Bad Request, 401 Unauthorized, 403 Forbidden
*   **Get all user's flashcards**
    *   Metoda: `GET`
    *   Ścieżka: `/`
    *   Opis: Pobiera wszystkie zapisane fiszki użytkownika.
    *   Response Payload: `FlashcardListDto` (`flashcards`: List<`FlashcardDto`>)
    *   Kody sukcesu: 200 OK
    *   Kody błędów: 401 Unauthorized
*   **Update a flashcard**
    *   Metoda: `PUT`
    *   Ścieżka: `/{flashcardId}`
    *   Opis: Aktualizuje istniejącą fiszkę.
    *   Request Payload: `FlashcardUpdateDto` (`frontContent`, `backContent`)
    *   Response Payload: `FlashcardDto`
    *   Kody sukcesu: 200 OK
    *   Kody błędów: 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found
*   **Delete a flashcard**
    *   Metoda: `DELETE`
    *   Ścieżka: `/{flashcardId}`
    *   Opis: Usuwa fiszkę.
    *   Response Payload: Brak
    *   Kody sukcesu: 204 No Content
    *   Kody błędów: 401 Unauthorized, 403 Forbidden, 404 Not Found

### 3.4. `ReviewSessionController`

*   Endpoint bazowy: `/api/review-sessions`
*   Zależności: `ReviewSessionService`

*   **Start a review session**
    *   Metoda: `POST`
    *   Ścieżka: `/`
    *   Opis: Tworzy nową sesję powtórkową z losowo wybranymi fiszkami.
    *   Request Payload: Brak
    *   Response Payload: `ReviewSessionDto` (`flashcards`: List<`FlashcardReviewDto`>)
    *   Kody sukcesu: 200 OK
    *   Kody błędów: 401 Unauthorized, 404 Not Found

## 4. Authentication and Authorization (JWT-Based - Na razie bez Spring Security)

*   **Mechanizm:** Uwierzytelnianie oparte o JSON Web Tokens (JWT).
*   **Struktura Tokenów:**
    *   Access Token: Krótko żyjący JWT z danymi użytkownika (np. ID, email).
    *   Refresh Token: Dłużej żyjący token do uzyskiwania nowych Access Tokenów.
*   **Założenia (do implementacji później ze Spring Security):**
    *   Ważność Access Token: 1 godzina.
    *   Ważność Refresh Token: 7 dni.
    *   Podpisywanie tokenów bezpiecznym algorytmem (np. HMAC SHA-256).
    *   Komunikacja tylko przez HTTPS.
*   **Przepływ (docelowy):**
    *   Rejestracja/logowanie zwraca Access i Refresh Token.
    *   Żądania API zawierają Access Token w nagłówku `Authorization: Bearer <token>`.
    *   Po wygaśnięciu Access Token, używany jest Refresh Token do uzyskania nowego.
    *   Możliwość unieważnienia tokenów (logout).

## 5. Validation and Business Logic

### 5.1. Zasady Walidacji (do zaimplementowania w DTO i/lub kontrolerach)

*   **Users:**
    *   Email: Poprawny format, unikalny.
    *   Hasło: Spełnia wymagania bezpieczeństwa (np. min. 8 znaków, litery, cyfry, znaki specjalne).
*   **Flashcards:**
    *   `frontContent`: Max 500 znaków.
    *   `backContent`: Max 200 znaków.
    *   `sourceType`: Jedna z wartości: `ai-full`, `ai-edited`, `manual`.
*   **Generations:**
    *   `sourceText`: Max 10,000 znaków, nie może być pusty.
*   **Review Sessions:**
    *   Domyślny zakres 5-15 losowo wybranych fiszek.

### 5.2. Logika Biznesowa (do zaimplementowania w serwisach)

*   **Generowanie Fiszki:** Proces zapisuje metadane, ale nie zapisuje automatycznie fiszek. Akceptacja odbywa się po stronie frontendu. Fiszki są zapisywane dopiero po wywołaniu endpointu `POST /api/flashcards`.
*   **Zarządzanie Fiszami:** Użytkownicy zarządzają tylko własnymi fiszkami (wymaga RLS lub logiki w serwisie). Aktualizacja fiszki pochodzącej z AI zmienia jej `sourceType` na `ai-edited`. Znaczniki czasu modyfikacji są aktualizowane.
*   **System Powtórek:** Backend dostarcza losowo wybraną listę fiszek. Frontend zarządza nawigacją.
*   **Row Level Security (RLS):** Aplikacja powinna ustawiać kontekst sesji bazy danych dla każdego uwierzytelnionego żądania. Zapytania bazodanowe powinny uwzględniać kontekst użytkownika. (Szczegóły w sekcji 6).
*   **Śledzenie Błędów:** Błędy generowania powinny być rejestrowane.

## 6. Implementacja Row Level Security (RLS)

*   **(Do uzupełnienia)** Szczegółowy opis sposobu ustawiania kontekstu użytkownika na poziomie sesji bazy danych (np. przez `SET app.current_user_id = ...` przed wykonaniem zapytań w transakcji).
*   **(Do uzupełnienia)** Sposób integracji z warstwą serwisową/repozytorium (np. poprzez dedykowany filtr, AOP, lub bezpośrednie wywołania w metodach serwisowych).

## 7. Integracja z AI dla generowania fiszek (Stub)

*   **(Do zdefiniowania)** Prosty interfejs `AiService` z metodą np. `generateFlashcards(String sourceText): List<FlashcardSuggestionDto>`.
*   **(Do zaimplementowania)** Implementacja `AiServiceStub`, która zwraca stałą, przykładową listę sugestii fiszek, symulując opóźnienie.

## 8. Plan implementacji krok po kroku

*   **(Do uzupełnienia)** Kolejność implementacji komponentów (sugerowana: Encje -> Repozytoria -> Mappery -> Serwisy (z RLS i AI stub) -> Kontrolery).
*   **(Do zidentyfikowania)** Krytyczne punkty wymagające szczególnej uwagi (np. implementacja RLS, zarządzanie transakcjami, mapowanie DTO).