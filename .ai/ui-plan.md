# Architektura UI dla 10x Flashcards

## 1. Przegląd struktury UI

Architektura UI dla 10x Flashcards to aplikacja Single Page Application (SPA) oparta na Angular 19.2 z Angular Material jako głównym zestawem komponentów. Aplikacja wykorzystuje NgRx do zarządzania stanem, z wdrożeniem wzorca fasady do izolacji komponentów od szczegółów implementacji stanu.

Struktura aplikacji jest minimalistyczna i składa się z pięciu głównych widoków:

- Logowanie i rejestracja (jako osobne ścieżki)
- Generowanie fiszek (główny widok aplikacji)
- Lista zapisanych fiszek
- Sesja powtórkowa

Zgodnie z wymaganiami, interfejs jest uproszczony bez złożonej nawigacji - wszystkie główne funkcje są dostępne bezpośrednio z panelu operacji w głównym widoku aplikacji.

## 2. Lista widoków

### 2.1. Widok logowania

- **Ścieżka widoku:** `/login`
- **Główny cel:** Umożliwienie użytkownikowi zalogowania się do systemu
- **Kluczowe informacje:** Formularz logowania z polami email i hasło
- Kluczowe komponenty:
  - Formularz logowania (Angular Reactive Forms)
  - Pola tekstowe email i hasło (MatFormField, MatInput)
  - Przycisk logowania (MatButton)
  - Link do widoku rejestracji
  - Komunikaty błędów (MatError)
- UX, dostępność i bezpieczeństwo:
  - Walidacja formularza z informacyjnymi komunikatami
  - Focus automatycznie na polu email przy ładowaniu
  - Obsługa klawisza Enter w polach formularza
  - Blokowanie przycisku podczas przetwarzania
  - Komunikaty błędów dostępne dla czytników ekranu

### 2.2. Widok rejestracji

- **Ścieżka widoku:** `/register`
- **Główny cel:** Umożliwienie utworzenia nowego konta użytkownika
- **Kluczowe informacje:** Formularz rejestracji z polami email, hasło, potwierdzenie hasła
- Kluczowe komponenty:
  - Formularz rejestracji (Angular Reactive Forms)
  - Pola tekstowe (MatFormField, MatInput)
  - Przycisk rejestracji (MatButton)
  - Link do widoku logowania
  - Komunikaty błędów (MatError)
- UX, dostępność i bezpieczeństwo:
  - Walidacja pól w czasie rzeczywistym
  - Wskaźnik siły hasła
  - Weryfikacja zgodności hasła i potwierdzenia
  - Komunikaty błędów dostępne dla czytników ekranu

### 2.3. Widok generowania fiszek

- **Ścieżka widoku:** `/generate` (domyślny widok po zalogowaniu)
- **Główny cel:** Generowanie fiszek z wprowadzonego tekstu
- Kluczowe informacje:
  - Pole tekstowe z licznikiem znaków (max 10 000)
  - Panel operacji z przyciskami
  - Siatka wygenerowanych fiszek
- Kluczowe komponenty:
  - Pole tekstowe z licznikiem (MatFormField, MatInput, MatHint)
  - Panel przycisków (MatButtonGroup)
  - Siatka fiszek (MatGridList)
  - Karty fiszek (MatCard)
  - Przyciski akcji dla fiszek (MatButton, MatIcon)
  - Spinner ładowania (MatProgressSpinner)
  - Komunikaty błędów (MatSnackBar)
- UX, dostępność i bezpieczeństwo:
  - Responsywna siatka dostosowująca się do szerokości ekranu
  - Wyraźne wskaźniki stanu (ładowanie, sukces, błąd)
  - Edycja inline bez dodatkowych okien modalnych
  - Intuicyjne przyciski akcji dla każdej fiszki

### 2.4. Widok listy fiszek

- **Ścieżka widoku:** `/flashcards`
- **Główny cel:** Przeglądanie i zarządzanie zapisanymi fiszkami
- **Kluczowe informacje:** Lista zapisanych fiszek z opcjami edycji i usuwania
- Kluczowe komponenty:
  - Lista fiszek (MatGridList)
  - Karty fiszek (MatCard)
  - Przyciski edycji i usuwania (MatButton, MatIcon)
  - Pola edycji inline (MatFormField, MatInput)
  - Przycisk powrotu do generowania
- UX, dostępność i bezpieczeństwo:
  - Responsywny układ listy
  - Edycja inline bez dodatkowych okien
  - Dialog potwierdzenia przed usunięciem (MatDialog)
  - Wskaźniki ładowania podczas operacji CRUD

### 2.5. Widok sesji powtórkowej

- **Ścieżka widoku:** `/review`
- **Główny cel:** Powtarzanie zapisanych fiszek
- Kluczowe informacje:
  - Aktualna fiszka (przód i tył)
  - Przyciski nawigacji
  - Wskaźnik postępu
- Kluczowe komponenty:
  - Karta fiszki (MatCard)
  - Obszary przedniej i tylnej strony
  - Zamazane pole odpowiedzi
  - Przyciski nawigacji (MatButton)
  - Wskaźnik postępu (MatProgressBar)
- UX, dostępność i bezpieczeństwo:
  - Wyraźne rozróżnienie pytania i odpowiedzi
  - Intuicyjny mechanizm odkrywania odpowiedzi po kliknięciu
  - Łatwa nawigacja przez przyciski "Poprzedni" i "Następny"
  - Wyraźne oznaczenie zakończenia sesji

## 3. Mapa podróży użytkownika

### 3.1. Tworzenie konta i pierwsze logowanie

1. Użytkownik wchodzi na stronę główną aplikacji
2. System przekierowuje do widoku logowania
3. Użytkownik klika link "Zarejestruj się"
4. Użytkownik wypełnia formularz rejestracji i zatwierdza
5. Po pomyślnej rejestracji system przekierowuje do widoku logowania
6. Użytkownik wprowadza dane logowania i zatwierdza
7. Po pomyślnym logowaniu system przekierowuje do widoku generowania fiszek

### 3.2. Generowanie i zarządzanie fiszkami

1. Użytkownik wprowadza tekst do pola tekstowego
2. Użytkownik klika przycisk "Generuj"
3. System wyświetla spinner ładowania
4. Po zakończeniu generowania system wyświetla propozycje fiszek w siatce
5. Użytkownik może wykonać następujące akcje dla każdej fiszki:
   - Zaakceptować fiszkę (przycisk "Akceptuj")
   - Edytować fiszkę (przycisk "Edytuj", edycja inline, zatwierdzenie)
   - Odrzucić fiszkę (przycisk "Odrzuć")
6. Użytkownik może użyć przycisków masowych akcji:
   - "Akceptuj wszystkie" - zapisanie wszystkich fiszek
   - "Akceptuj wybrane" - zapisanie wybranych fiszek
   - "Odrzuć wszystko" - odrzucenie wszystkich fiszek
7. Po zapisaniu fiszek użytkownik może:
   - Wprowadzić nowy tekst i wygenerować więcej fiszek
   - Przejść do widoku listy fiszek
   - Rozpocząć sesję powtórkową

### 3.3. Przeglądanie i edycja zapisanych fiszek

1. Użytkownik klika przycisk "Lista fiszek" w panelu operacji
2. System wyświetla listę zapisanych fiszek
3. Użytkownik może:
   - Edytować fiszkę (edycja inline)
   - Usunąć fiszkę (z potwierdzeniem)
4. Po zakończeniu zarządzania użytkownik klika przycisk powrotu do generowania

### 3.4. Sesja powtórkowa

1. Użytkownik klika przycisk "Sesja powtórkowa" w panelu operacji
2. System losowo wybiera 5-15 fiszek
3. System wyświetla pierwszą fiszkę z ukrytą odpowiedzią
4. Użytkownik klika obszar odpowiedzi, aby ją odkryć
5. Użytkownik nawiguje między fiszkami używając przycisków:
   - "Poprzedni" - przejście do poprzedniej fiszki
   - "Następny" - przejście do następnej fiszki
6. Na ostatniej fiszce przycisk "Następny" zmienia się na "Zakończ"
7. Po kliknięciu "Zakończ" system przekierowuje do widoku generowania

## 4. Układ i struktura nawigacji

Zgodnie z wymaganiami i decyzjami z sesji, architektura UI została zaprojektowana z uproszczonym układem bez złożonej nawigacji.

### 4.1. Routing główny

- `/login` - widok logowania
- `/register` - widok rejestracji
- `/generate` - widok generowania fiszek (domyślny po zalogowaniu)
- `/flashcards` - widok listy fiszek
- `/review` - widok sesji powtórkowej

### 4.2. Guard uwierzytelniania

- Wszystkie ścieżki poza `/login` i `/register` są chronione przez AuthGuard
- Niezalogowani użytkownicy są automatycznie przekierowywani do `/login`

### 4.3. Panel operacji

Panel operacji w widoku generowania służy jako główny element nawigacji aplikacji:

- Przycisk "Generuj" - generowanie fiszek z wprowadzonego tekstu
- Przycisk "Akceptuj wszystkie" - zapisanie wszystkich fiszek
- Przycisk "Akceptuj wybrane" - zapisanie wybranych fiszek
- Przycisk "Odrzuć wszystko" - odrzucenie wszystkich fiszek
- Przycisk "Sesja powtórkowa" - przejście do widoku sesji powtórkowej
- Przycisk "Lista fiszek" - przejście do widoku listy fiszek

### 4.4. Nawigacja w sesji powtórkowej

- Przycisk "Poprzedni" - przejście do poprzedniej fiszki
- Przycisk "Następny" - przejście do następnej fiszki
- Przycisk "Zakończ" - zakończenie sesji i powrót do widoku generowania

## 5. Kluczowe komponenty

### 5.1. FlashcardCardComponent

- **Opis:** Reużywalny komponent do wyświetlania pojedynczej fiszki
- Cechy:
  - Prezentacja przedniej i tylnej strony fiszki
  - Tryb edycji inline
  - Przyciski akcji (akceptuj, edytuj, odrzuć)
  - Obsługa trybu powtórek z zamazaną odpowiedzią

### 5.2. FlashcardGridComponent

- **Opis:** Komponent do wyświetlania siatki fiszek
- Cechy:
  - Responsywny układ (3-4 kolumn)
  - Obsługa wyboru fiszek (dla "Akceptuj wybrane")
  - Renderowanie FlashcardCardComponent dla każdej fiszki

### 5.3. TextInputComponent

- **Opis:** Komponent pola tekstowego do wprowadzania treści
- Cechy:
  - Licznik znaków (max 10 000)
  - Walidacja długości
  - Autoresize

### 5.4. OperationsPanelComponent

- **Opis:** Panel operacji z przyciskami akcji
- Cechy:
  - Dynamiczne aktywowanie/dezaktywowanie przycisków
  - Obsługa wszystkich głównych operacji

### 5.5. ReviewNavigationComponent

- **Opis:** Komponent nawigacji w trybie powtórek
- Cechy:
  - Przyciski "Poprzedni" i "Następny"/"Zakończ"
  - Wskaźnik postępu

### 5.6. LoadingSpinnerComponent

- **Opis:** Wskaźnik ładowania
- Cechy:
  - Prosty spinner Angular Material
  - Opcjonalne komunikaty

### 5.7. HttpInterceptorService

- **Opis:** Interceptor HTTP do obsługi uwierzytelniania
- Cechy:
  - Automatyczne dodawanie tokenu JWT
  - Automatyczne odświeżanie tokenu
  - Obsługa błędów 401/403

### 5.8. AuthFacade

- **Opis:** Fasada NgRx do operacji uwierzytelniania
- Cechy:
  - Abstrakcja stanu uwierzytelniania
  - Metody do logowania, rejestracji, wylogowania

### 5.9. FlashcardsFacade

- **Opis:** Fasada NgRx do operacji na fiszkach
- Cechy:
  - Abstrakcja stanu fiszek
  - Metody do generowania, zapisywania, aktualizacji, usuwania

### 5.10. ReviewSessionFacade

- **Opis:** Fasada NgRx do sesji powtórkowych
- Cechy:
  - Abstrakcja stanu sesji powtórkowej
  - Metody do rozpoczynania sesji, nawigacji między fiszkami