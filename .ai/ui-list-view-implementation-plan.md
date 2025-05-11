# Plan implementacji widoku listy fiszek

## 1. Przegląd

Widok listy fiszek umożliwia użytkownikowi przeglądanie, edycję i usuwanie zapisanych fiszek. Widok prezentuje fiszki w responsywnej siatce (3-4 kolumn) z możliwością edycji inline i usuwania po potwierdzeniu. Każdy użytkownik ma dostęp tylko do swoich własnych fiszek dzięki mechanizmowi Row Level Security.

## 2. Routing widoku

- Ścieżka: `/flashcards`
- Guard: AuthGuard (wymaga uwierzytelnienia)
- Parent component: none (standalone view)
- Navigation: dostępny z głównego widoku generowania przez przycisk "Lista fiszek"

## 3. Struktura komponentów

```
FlashcardsListComponent (main container)
├── FlashcardsListHeaderComponent
│   ├── h1 (tytuł widoku)
│   └── MatButton (powrót do generowania)
├── @if (loading)
│   └── LoadingStateComponent
├── @if (error)
│   └── ErrorStateComponent
├── @if (flashcards.length === 0)
│   └── EmptyStateComponent
├── @if (flashcards.length > 0)
│   └── FlashcardsGridComponent
│       └── @for (flashcard of flashcards)
│           └── FlashcardItemComponent
│               ├── FlashcardContentComponent
│               └── FlashcardActionsComponent
```

## 4. Szczegóły komponentów

### FlashcardsListComponent

- Opis komponentu: Główny kontener widoku listy fiszek, zarządza ładowaniem danych i nawigacją
- Główne elementy:
  - FlashcardsListHeaderComponent
  - FlashcardsGridComponent (warunkowy)
  - LoadingStateComponent (warunkowy)
  - ErrorStateComponent (warunkowy)
  - EmptyStateComponent (warunkowy)
- Obsługiwane interakcje:
  - OnInit - ładowanie fiszek
  - Nawigacja do /generate
  - Aktualizacja stanu ładowania/błędu
- Obsługiwana walidacja: brak (tylko prezentacja)
- Typy:
  - flashcards: signal<FlashcardDto[]>()
  - loading: signal<boolean>()
  - error: signal<string | null>()
- Propsy: brak (root component)

### FlashcardsListHeaderComponent

- Opis komponentu: Nagłówek widoku z tytułem i przyciskiem powrotu
- Główne elementy:
  - `<h1>Moje fiszki</h1>`
  - `<button mat-raised-button>Powrót do generowania</button>`
- Obsługiwane interakcje:
  - Kliknięcie przycisku powrotu → navigateToGenerate()
- Obsługiwana walidacja: brak
- Typy: brak specjalnych typów
- Propsy: brak

### FlashcardsGridComponent

- Opis komponentu: Responsywna siatka fiszek (3-4 kolumn w zależności od szerokości ekranu)
- Główne elementy:
  - `<mat-grid-list [cols]="columns" gutterSize="16px">`
  - `<mat-grid-tile>` dla każdej fiszki
- Obsługiwane interakcje:
  - Renderowanie listy fiszek
  - Obsługa responsywności (zmiana liczby kolumn)
- Obsługiwana walidacja: brak
- Typy:
  - columns: signal<number>()
  - flashcards: FlashcardDto[]
- Propsy:
  - @Input() flashcards: FlashcardDto[]
  - @Output() editFlashcard: EventEmitter<FlashcardDto>
  - @Output() deleteFlashcard: EventEmitter<string>

### FlashcardItemComponent

- Opis komponentu: Pojedyncza karta fiszki z opcjami edycji i usuwania
- Główne elementy:
  - `<mat-card>`
  - FlashcardContentComponent
  - FlashcardActionsComponent
- Obsługiwane interakcje:
  - Przełączanie między trybem wyświetlania a edycją
  - Przekazywanie zdarzeń do parent component
- Obsługiwana walidacja:
  - Sprawdzenie czy jest w trybie edycji
  - Walidacja długości content przy zapisie
- Typy:
  - isEditing: boolean
  - editForm: FormGroup
- Propsy:
  - @Input() flashcard: FlashcardDto
  - @Input() isEditing: boolean
  - @Output() edit: EventEmitter<void>
  - @Output() save: EventEmitter<FlashcardEditModel>
  - @Output() delete: EventEmitter<void>
  - @Output() cancel: EventEmitter<void>

### FlashcardContentComponent

- Opis komponentu: Prezentacja lub edycja treści fiszki
- Główne elementy:
  - `<div>` dla trybu wyświetlania
  - `<form>` dla trybu edycji z MatFormField
  - Walidatory dla długości tekstu
- Obsługiwane interakcje:
  - Przełączanie między trybami
  - Walidacja formularza podczas edycji
- Obsługiwana walidacja:
  - frontContent: maksymalnie 500 znaków, wymagane
  - backContent: maksymalnie 200 znaków, wymagane
  - Wyświetlanie błędów walidacji
- Typy:
  - editForm: FormGroup
  - validationErrors: ValidationErrors
- Propsy:
  - @Input() flashcard: FlashcardDto
  - @Input() isEditing: boolean
  - @Input() form: FormGroup
  - @Output() formSubmit: EventEmitter<void>

### FlashcardActionsComponent

- Opis komponentu: Przyciski akcji dla fiszki (edit, save, delete, cancel)
- Główne elementy:
  - `<button mat-icon-button>` dla każdej akcji
  - `<mat-icon>` dla ikon
- Obsługiwane interakcje:
  - Kliknięcie Edit → edytuj fiszkę
  - Kliknięcie Save → zapisz zmiany
  - Kliknięcie Delete → usuń fiszkę (z potwierdzeniem)
  - Kliknięcie Cancel → anuluj edycję
- Obsługiwana walidacja:
  - Disable Save jeśli formularz jest invalid
- Typy: brak specjalnych typów
- Propsy:
  - @Input() isEditing: boolean
  - @Input() canSave: boolean
  - @Output() edit: EventEmitter<void>
  - @Output() save: EventEmitter<void>
  - @Output() delete: EventEmitter<void>
  - @Output() cancel: EventEmitter<void>

## 5. Typy

```typescript
// Core flashcard type from API
interface FlashcardDto {
  id: string;
  frontContent: string;
  backContent: string;
  sourceType: 'ai-full' | 'ai-edited' | 'manual';
  lastModifiedAt: string;
}

// Update payload for API
interface FlashcardUpdateDto {
  frontContent: string;
  backContent: string;
}

// API response types
interface FlashcardsListResponse {
  flashcards: FlashcardDto[];
}

interface FlashcardUpdateResponse {
  id: string;
  frontContent: string;
  backContent: string;
  sourceType: 'ai-edited';
  lastModifiedAt: string;
}

// View model types
interface FlashcardEditState {
  flashcardId: string;
  isEditing: boolean;
  editForm: FormGroup;
  isSaving: boolean;
}

interface FlashcardsListState {
  flashcards: FlashcardDto[];
  loading: boolean;
  error: string | null;
  editingFlashcards: Map<string, FlashcardEditState>;
}

// Dialog types
interface DeleteConfirmationData {
  flashcardId: string;
  frontContent: string;
}

interface DeleteConfirmationResult {
  confirmed: boolean;
}
```

## 6. Zarządzanie stanem

State będzie zarządzany przez NgRx z wykorzystaniem wzorca fasady:

```typescript
// FlashcardsFacade service
@Injectable({
  providedIn: 'root'
})
export class FlashcardsFacade {
  // Selectors to signals
  flashcards$ = this.store.select(selectAllFlashcards).pipe(
    toSignal({ initialValue: [] })
  );
  
  loading$ = this.store.select(selectFlashcardsLoading).pipe(
    toSignal({ initialValue: false })
  );
  
  error$ = this.store.select(selectFlashcardsError).pipe(
    toSignal({ initialValue: null })
  );
  
  // Public methods
  loadFlashcards(): void
  updateFlashcard(id: string, data: FlashcardUpdateDto): void
  deleteFlashcard(id: string): void
  toggleEdit(id: string): void
  clearError(): void
}
```

## 7. Integracja API

```typescript
// FlashcardsApiService
@Injectable({
  providedIn: 'root'
})
export class FlashcardsApiService {
  // GET /api/flashcards
  getFlashcards(): Observable<FlashcardsListResponse> {
    return this.http.get<FlashcardsListResponse>('/api/flashcards');
  }
  
  // PUT /api/flashcards/{flashcardId}
  updateFlashcard(id: string, data: FlashcardUpdateDto): Observable<FlashcardUpdateResponse> {
    return this.http.put<FlashcardUpdateResponse>(`/api/flashcards/${id}`, data);
  }
  
  // DELETE /api/flashcards/{flashcardId}
  deleteFlashcard(id: string): Observable<void> {
    return this.http.delete<void>(`/api/flashcards/${id}`);
  }
}
```

## 8. Interakcje użytkownika

1. **Wejście na widok**: automatyczne załadowanie listy fiszek

2. Kliknięcie "Edytuj"

   :

   - Aktywacja trybu edycji dla wybranej fiszki
   - Wypełnienie formularza obecnymi wartościami
   - Ukrycie akcji usuwania

3. Kliknięcie "Zapisz"

   :

   - Walidacja formularza
   - Wysłanie PUT request do API
   - Aktualizacja stanu na success/error
   - Powrót do trybu wyświetlania

4. Kliknięcie "Anuluj"

   :

   - Powrót do trybu wyświetlania
   - Odrzucenie zmian

5. Kliknięcie "Usuń"

   :

   - Otwarcie dialogu potwierdzenia
   - Na potwierdzenie: wysłanie DELETE request
   - Usunięcie fiszki z listy

6. **Kliknięcie "Powrót"**: nawigacja do widoku generowania

## 9. Warunki i walidacja

### Na poziomie formularza (FlashcardContentComponent):

```typescript
this.editForm = this.formBuilder.group({
  frontContent: [
    this.flashcard.frontContent,
    [
      Validators.required,
      Validators.maxLength(500)
    ]
  ],
  backContent: [
    this.flashcard.backContent,
    [
      Validators.required,
      Validators.maxLength(200)
    ]
  ]
});
```

### Na poziomie API:

- 400 Bad Request → wyświetlenie komunikatu o błędnej walidacji
- 401 Unauthorized → przekierowanie do logowania
- 403 Forbidden → komunikat o braku uprawnień

### Warunki renderowania w template:

```html
@if (loading()) {
  <LoadingStateComponent />
} @else if (error()) {
  <ErrorStateComponent [error]="error()" />
} @else if (flashcards().length === 0) {
  <EmptyStateComponent />
} @else {
  <FlashcardsGridComponent [flashcards]="flashcards()" />
}
```

## 10. Obsługa błędów

### Network Errors:

- Wyświetlenie komunikatu o braku połączenia
- Możliwość ponownego załadowania

### Authentication Errors:

- 401: przekierowanie do strony logowania
- 403: komunikat o braku uprawnień

### Validation Errors:

- Wyświetlenie konkretnych błędów walidacji pod polami
- Blokowanie przycisku save do czasu poprawienia

### Server Errors:

- 500: ogólny komunikat błędu serwera
- 404: komunikat o nieznalezionej fiszce

### Implementation przykład:

```typescript
try {
  await this.flashcardsFacade.updateFlashcard(id, data);
  this.snackBar.open('Fiszka została zaktualizowana', 'OK', {
    duration: 3000
  });
} catch (error) {
  if (error.status === 401) {
    this.router.navigate(['/login']);
  } else if (error.status === 403) {
    this.snackBar.open('Brak uprawnień do edycji tej fiszki', 'OK');
  } else {
    this.snackBar.open('Wystąpił błąd podczas zapisywania', 'OK');
  }
}
```

## 11. Kroki implementacji

1. **Utworzenie struktury komponentów**:

   - Generacja komponentów przez Angular CLI
   - Ustawienie OnPush change detection
   - Dodanie standalone: true

2. **Implementacja NgRx state management**:

   - Utworzenie actions, reducers, effects
   - Implementacja FlashcardsFacade
   - Przygotowanie selectors

3. **Integracja z API**:

   - Utworzenie FlashcardsApiService
   - Implementacja HTTP calls z proper error handling
   - Dodanie interceptora dla JWT

4. **Implementacja komponentów UI**:

   - FlashcardsListComponent z podstawową logiką
   - FlashcardsGridComponent z responsive layout
   - FlashcardItemComponent z edycją inline
   - FlashcardActionsComponent z przyciskami

5. **Dodanie walidacji**:

   - Implementacja reactive forms
   - Dodanie validatorów
   - Wyświetlanie błędów walidacji

6. **Implementacja responsywności**:

   - Użycie BreakpointObserver
   - Dostosowanie liczby kolumn do rozmiaru ekranu
   - Testowanie na różnych rozmiarach

7. **Obsługa błędów**:

   - Dodanie ErrorStateComponent
   - Implementacja error handling w effects
   - Dodanie notyfikacji przez MatSnackBar

8. **Stylowanie i UX**:

   - Dodanie Angular Material theming
   - Implementacja animacji przejść
   - Dodanie loading indicators

9. **Testowanie**:

   - Unit testy dla komponentów

10. **Optymalizacja wydajności**:

    - Implementacja trackBy function

      