# Plan implementacji widoku generowania fiszek

## 1. Przegląd

Widok generowania fiszek umożliwia użytkownikom wprowadzanie tekstu (do 10 000 znaków), na podstawie którego system
generuje propozycje fiszek przy użyciu algorytmu AI. Użytkownik może następnie akceptować, edytować lub odrzucać
wygenerowane propozycje. Widok ten jest domyślnym ekranem po zalogowaniu do aplikacji.

## 2. Routing widoku

Widok będzie dostępny pod ścieżką `/generate` i jest już skonfigurowany jako domyślny widok po zalogowaniu w pliku
app.routes.ts.

## 3. Struktura komponentów

```
GenerateComponent (kontener główny)
├── TextInputComponent (wprowadzanie tekstu)
├── ActionPanelComponent (przyciski akcji)
└── FlashcardGridComponent (siatka fiszek)
    └── FlashcardItemComponent (pojedyncza fiszka w siatce)
```

## 4. Szczegóły komponentów

### GenerateComponent

- **Opis komponentu:** Główny kontener widoku generowania fiszek, koordynujący pracę wszystkich podkomponentów i
  zarządzający stanem widoku.
- **Główne elementy:** Container z podkomponentami TextInputComponent, ActionPanelComponent, FlashcardGridComponent oraz
  MatProgressSpinner do wyświetlania stanu ładowania.
- **Obsługiwane zdarzenia:**
    - Inicjalizacja widoku
    - Obsługa generowania fiszek
    - Zapisywanie zaakceptowanych fiszek
    - Wyświetlanie komunikatów o błędach
- **Walidacja:**
    - Tekst nie może być pusty przy generowaniu
    - Co najmniej jedna fiszka musi być zaakceptowana przy zapisie
- **Typy:**
    - `GenerateState` - model stanu komponentu
    - `GenerateFlashcardsRequest` i `GenerateFlashcardsResponse` z API
    - `SaveFlashcardsRequest` i `SaveFlashcardsResponse` z API
- **Propsy:** Brak (komponent główny)

### TextInputComponent

- **Opis komponentu:** Komponent odpowiedzialny za wprowadzanie tekstu źródłowego do generowania fiszek.
- **Główne elementy:** MatFormField, MatInput i MatHint z licznikiem znaków (max 10 000).
- **Obsługiwane zdarzenia:**
    - Zmiana zawartości pola tekstowego (textChange)
    - Walidacja wprowadzonego tekstu
- **Walidacja:**
    - Limit 10 000 znaków
    - Pole nie może być puste przy generowaniu
- **Typy:**
    - `TextInputConfig` - konfiguracja komponentu
- **Propsy:**
    - `value: string` - aktualny tekst
    - `maxLength: number` - maksymalna długość tekstu (domyślnie 10000)
    - `placeholder: string` - podpowiedź w polu
    - `textChange: EventEmitter<string>` - emituje zmieniony tekst
    - `disabled: boolean` - czy pole jest wyłączone

### ActionPanelComponent

- **Opis komponentu:** Panel z przyciskami do wykonywania akcji na wprowadzonym tekście oraz nawigacji do innych
  widoków.
- **Główne elementy:** MatButtonGroup z przyciskami akcji (Generuj, Wyczyść, Akceptuj wszystkie, Akceptuj wybrane,
  Odrzuć wszystko) oraz nawigacji (Lista fiszek, Sesja powtórkowa).
- **Obsługiwane zdarzenia:**
    - Kliknięcie przycisku generowania (generate)
    - Kliknięcie przycisku czyszczenia (clear)
    - Kliknięcie przycisku zapisu wszystkich fiszek (saveAll)
    - Kliknięcie przycisku akceptacji wybranych fiszek (saveSelected)
    - Kliknięcie przycisku odrzucenia wszystkich fiszek (rejectAll)
    - Kliknięcie przycisku nawigacji do listy fiszek (navigateToList)
    - Kliknięcie przycisku nawigacji do sesji powtórkowej (navigateToReview)
- **Walidacja:**
    - Przycisk generowania wyłączony, gdy nie ma tekstu
    - Przycisk zapisu wyłączony, gdy nie ma fiszek
    - Przycisk zapisu wybranych wyłączony, gdy nie ma wybranych fiszek
- **Typy:**
    - `ActionPanelConfig` - konfiguracja komponentu
- **Propsy:**
    - `isGenerating: boolean` - czy trwa generowanie fiszek
    - `hasText: boolean` - czy jest tekst do generowania
    - `hasSuggestions: boolean` - czy są wygenerowane fiszki
    - `hasSelectedFlashcards: boolean` - czy są zaznaczone fiszki
    - `selectionMode: boolean` - czy tryb wyboru jest aktywny
    - `onGenerate: EventEmitter<void>` - emitowane przy kliknięciu przycisku generowania
    - `onClear: EventEmitter<void>` - emitowane przy kliknięciu przycisku czyszczenia
    - `onSaveAll: EventEmitter<void>` - emitowane przy kliknięciu przycisku zapisu wszystkich fiszek
    - `onSaveSelected: EventEmitter<void>` - emitowane przy kliknięciu przycisku zapisu wybranych fiszek
    - `onRejectAll: EventEmitter<void>` - emitowane przy kliknięciu przycisku odrzucenia wszystkich fiszek
    - `onNavigateToList: EventEmitter<void>` - emitowane przy kliknięciu przycisku nawigacji do listy fiszek
    - `onNavigateToReview: EventEmitter<void>` - emitowane przy kliknięciu przycisku nawigacji do sesji powtórkowej

### FlashcardGridComponent

- **Opis komponentu:** Siatka wyświetlająca wygenerowane fiszki w formie kart z możliwością wyboru wielu fiszek.
- **Główne elementy:** MatGridList z responsywną siatką kart (3-4 kolumny zależnie od szerokości ekranu), mechanizm
  zaznaczania wielu fiszek.
- **Obsługiwane zdarzenia:**
    - Zaakceptowanie fiszki (acceptFlashcard)
    - Edycja fiszki (editFlashcard)
    - Odrzucenie fiszki (rejectFlashcard)
    - Wybór fiszki (selectFlashcard)
    - Wybór wszystkich fiszek (selectAllFlashcards)
    - Odznaczenie wszystkich fiszek (deselectAllFlashcards)
- **Walidacja:** Brak dodatkowej walidacji na poziomie siatki
- **Typy:**
    - `FlashcardViewModel` - model fiszki używany w widoku
- **Propsy:**
    - `flashcards: FlashcardViewModel[]` - lista fiszek do wyświetlenia
    - `loading: boolean` - czy trwa ładowanie fiszek
    - `selectionMode: boolean` - czy tryb wyboru jest aktywny
    - `selectedFlashcards: FlashcardViewModel[]` - lista wybranych fiszek
    - `onAccept: EventEmitter<FlashcardViewModel>` - emitowane przy akceptacji fiszki
    - `onReject: EventEmitter<FlashcardViewModel>` - emitowane przy odrzuceniu fiszki
    - `onEdit: EventEmitter<FlashcardViewModel>` - emitowane przy edycji fiszki
    - `onSelect: EventEmitter<{ flashcard: FlashcardViewModel, selected: boolean }>` - emitowane przy
      zaznaczeniu/odznaczeniu fiszki
    - `onSelectAll: EventEmitter<void>` - emitowane przy zaznaczeniu wszystkich fiszek
    - `onDeselectAll: EventEmitter<void>` - emitowane przy odznaczeniu wszystkich fiszek

### FlashcardItemComponent

- **Opis komponentu:** Pojedyncza karta fiszki z możliwością akceptacji, edycji, odrzucenia oraz zaznaczenia.
- **Główne elementy:** MatCard z treścią fiszki (przód/tył), przyciskami akcji oraz opcjonalnie checkboxem do
  zaznaczania.
- **Obsługiwane zdarzenia:**
    - Kliknięcie przycisku akceptacji (accept)
    - Kliknięcie przycisku edycji (edit)
    - Kliknięcie przycisku odrzucenia (reject)
    - Zapisanie edytowanej fiszki (save)
    - Anulowanie edycji fiszki (cancel)
    - Zaznaczenie/odznaczenie fiszki (select)
- **Walidacja:**
    - Przy edycji pola nie mogą być puste
- **Typy:**
    - `FlashcardViewModel` - model fiszki używany w widoku
    - `EditState` - stan edycji fiszki
- **Propsy:**
    - `flashcard: FlashcardViewModel` - dane fiszki
    - `isSelectable: boolean` - czy fiszka może być zaznaczona
    - `isSelected: boolean` - czy fiszka jest zaznaczona
    - `onAccept: EventEmitter<FlashcardViewModel>` - emitowane przy akceptacji fiszki
    - `onReject: EventEmitter<FlashcardViewModel>` - emitowane przy odrzuceniu fiszki
    - `onEdit: EventEmitter<FlashcardViewModel>` - emitowane przy zapisie edytowanej fiszki
    - `onSelect: EventEmitter<boolean>` - emitowane przy zaznaczeniu/odznaczeniu fiszki

## 5. Typy

```typescript
// Model fiszki używany w widoku
interface FlashcardViewModel {
  id?: string;
  frontContent: string;
  backContent: string;
  status: 'pending' | 'accepted' | 'rejected';
  sourceType: 'ai-full' | 'ai-edited' | 'manual';
  isEditing?: boolean;
  isSelected?: boolean;
}

// Model stanu komponentu generowania
interface GenerateState {
  sourceText: string;
  isGenerating: boolean;
  error: string | null;
  generationId: string | null;
  flashcards: FlashcardViewModel[];
  isSaving: boolean;
  selectionMode: boolean;
}

// Konfiguracja komponentu wprowadzania tekstu
interface TextInputConfig {
  value: string;
  maxLength: number;
  placeholder: string;
  disabled: boolean;
}

// Konfiguracja panelu akcji
interface ActionPanelConfig {
  isGenerating: boolean;
  hasText: boolean;
  hasSuggestions: boolean;
  hasSelectedFlashcards: boolean;
  selectionMode: boolean;
}

// Stan edycji fiszki
interface EditState {
  frontContent: string;
  backContent: string;
}
```

## 6. Zarządzanie stanem

Stan widoku będzie zarządzany w komponencie GenerateComponent z użyciem RxJS i możemy zdefiniować service do zarządzania
stanem:

```typescript
@Injectable()
export class GenerateStateService {
  private state = new BehaviorSubject<GenerateState>({
    sourceText: '',
    isGenerating: false,
    error: null,
    generationId: null,
    flashcards: [],
    isSaving: false,
    selectionMode: false
  });
  
  state$ = this.state.asObservable();
  
  updateState(newState: Partial<GenerateState>) {
    this.state.next({...this.state.value, ...newState});
  }
  
  get currentState(): GenerateState {
    return this.state.value;
  }
}
```

## 7. Integracja API

### Masowe operacje na fiszkach

```typescript
// Akceptacja wszystkich fiszek
acceptAllFlashcards() {
  const updatedFlashcards = this.state.value.flashcards.map(flashcard => ({
    ...flashcard,
    status: 'accepted'
  }));
  
  this.updateState({ flashcards: updatedFlashcards });
}

// Akceptacja wybranych fiszek
acceptSelectedFlashcards() {
  const updatedFlashcards = this.state.value.flashcards.map(flashcard => {
    if (flashcard.isSelected) {
      return {
        ...flashcard,
        status: 'accepted',
        isSelected: false
      };
    }
    return flashcard;
  });
  
  this.updateState({ 
    flashcards: updatedFlashcards,
    selectionMode: false 
  });
}

// Odrzucenie wszystkich fiszek
rejectAllFlashcards() {
  const updatedFlashcards = this.state.value.flashcards.map(flashcard => ({
    ...flashcard,
    status: 'rejected'
  }));
  
  this.updateState({ flashcards: updatedFlashcards });
}

// Przełączanie trybu wyboru
toggleSelectionMode() {
  const selectionMode = !this.state.value.selectionMode;
  
  // Resetujemy zaznaczenia przy wyłączeniu trybu wyboru
  let updatedFlashcards = this.state.value.flashcards;
  if (!selectionMode) {
    updatedFlashcards = updatedFlashcards.map(flashcard => ({
      ...flashcard,
      isSelected: false
    }));
  }
  
  this.updateState({ 
    selectionMode,
    flashcards: updatedFlashcards
  });
}

// Wybór/odznaczenie fiszki
toggleFlashcardSelection(flashcard: FlashcardViewModel, selected: boolean) {
  const updatedFlashcards = this.state.value.flashcards.map(f => {
    if (f === flashcard) {
      return {
        ...f,
        isSelected: selected
      };
    }
    return f;
  });
  
  this.updateState({ flashcards: updatedFlashcards });
}
```

### Nawigacja do innych widoków

```typescript
// Nawigacja do listy fiszek
navigateToFlashcardsList() {
  this.router.navigate(['/flashcards']);
}

// Nawigacja do sesji powtórkowej
navigateToReview() {
  this.router.navigate(['/review']);
}
```

### Generowanie fiszek

```typescript
generateFlashcards(sourceText: string) {
  this.updateState({ isGenerating: true, error: null });
  
  this.generationControllerService.generateFlashcards({
    body: { sourceText }
  }).pipe(
    finalize(() => this.updateState({ isGenerating: false }))
  ).subscribe({
    next: (response) => {
      const flashcards = response.suggestedFlashcards.map(card => ({
        frontContent: card.frontContent,
        backContent: card.backContent,
        status: 'pending',
        sourceType: 'ai-full'
      }));
      
      this.updateState({
        flashcards,
        generationId: response.generationId
      });
    },
    error: (error) => {
      this.updateState({ error: 'Nie udało się wygenerować fiszek. Spróbuj ponownie.' });
      this.snackBar.open('Wystąpił błąd podczas generowania fiszek', 'OK', {
        duration: 3000
      });
    }
  });
}
```

### Zapisywanie fiszek

```typescript
saveAcceptedFlashcards() {
  const acceptedFlashcards = this.state.value.flashcards.filter(f => f.status === 'accepted');
  
  if (acceptedFlashcards.length === 0) {
    this.snackBar.open('Brak zaakceptowanych fiszek do zapisania', 'OK', {
      duration: 3000
    });
    return;
  }
  
  this.updateState({ isSaving: true });
  
  this.flashcardControllerService.saveFlashcards({
    body: {
      generationId: this.state.value.generationId,
      flashcards: acceptedFlashcards.map(card => ({
        frontContent: card.frontContent,
        backContent: card.backContent,
        sourceType: card.sourceType
      }))
    }
  }).pipe(
    finalize(() => this.updateState({ isSaving: false }))
  ).subscribe({
    next: (response) => {
      this.snackBar.open(`Zapisano ${response.savedCount} fiszek`, 'OK', {
        duration: 3000
      });
      this.updateState({ 
        sourceText: '',
        flashcards: [],
        generationId: null
      });
    },
    error: (error) => {
      this.updateState({ error: 'Nie udało się zapisać fiszek. Spróbuj ponownie.' });
      this.snackBar.open('Wystąpił błąd podczas zapisywania fiszek', 'OK', {
        duration: 3000
      });
    }
  });
}
```

## 8. Interakcje użytkownika

1. **Wprowadzanie tekstu:**
    - Użytkownik wpisuje lub wkleja tekst w polu tekstowym
    - Tekst jest walidowany pod kątem długości (max 10 000 znaków)

2. **Operacje masowe:**
    - Akceptacja wszystkich fiszek: użytkownik klika przycisk "Akceptuj wszystkie" aby oznaczyć wszystkie fiszki jako
      zaakceptowane
    - Akceptacja wybranych fiszek:
        1. Użytkownik aktywuje tryb wyboru klikając przycisk "Zaznacz fiszki"
        2. Użytkownik zaznacza wybrane fiszki klikając na checkbox
        3. Użytkownik klika przycisk "Akceptuj zaznaczone"
    - Odrzucenie wszystkich fiszek: użytkownik klika przycisk "Odrzuć wszystkie" aby oznaczyć wszystkie fiszki jako
      odrzucone

3. **Nawigacja do innych widoków:**
    - Nawigacja do listy fiszek: użytkownik klika przycisk "Lista fiszek"
    - Nawigacja do sesji powtórkowej: użytkownik klika przycisk "Sesja powtórkowa"

4. **Generowanie fiszek:**
    - Użytkownik klika przycisk "Generuj"
    - System wyświetla spinner ładowania
    - Po zakończeniu generowania wyświetlane są fiszki w siatce

5. **Zarządzanie fiszkami:**
    - Akceptacja fiszki: kliknięcie przycisku z ikoną zatwierdzenia
    - Edycja fiszki: kliknięcie przycisku z ikoną edycji, które zmienia widok fiszki na edytowalny
    - Odrzucenie fiszki: kliknięcie przycisku z ikoną odrzucenia

6. **Edycja fiszki:**
    - Po kliknięciu przycisku edycji, fiszka zmienia się w formularz edycji
    - Użytkownik może edytować zawartość przodu i tyłu fiszki
    - Po zakończeniu edycji użytkownik klika przycisk "Zapisz" lub anuluje edycję

7. **Zapisywanie fiszek:**
    - Użytkownik może zapisać wszystkie zaakceptowane fiszki klikając przycisk "Zapisz wszystkie"
    - System wyświetla komunikat o liczbie zapisanych fiszek

## 9. Warunki i walidacja

1. **Walidacja pola tekstowego:**
    - Pole nie może być puste przy generowaniu fiszek
    - Maksymalna długość tekstu to 10 000 znaków

2. **Walidacja przy edycji fiszki:**
    - Zawartość przodu i tyłu fiszki nie może być pusta

3. **Walidacja przy zapisie fiszek:**
    - Przynajmniej jedna fiszka musi mieć status "zaakceptowana"
    - Generacja musi mieć przypisany identyfikator (generationId)

4. **Walidacja przy masowych operacjach:**
    - Akceptacja wybranych fiszek: przynajmniej jedna fiszka musi być zaznaczona
    - Tryb wyboru: przyciski akceptacji wszystkich i odrzucenia wszystkich są wyłączone

## 10. Obsługa błędów

1. **Błędy API:**
    - Błąd generowania fiszek: wyświetlenie komunikatu i możliwość ponownej próby
    - Błąd zapisywania fiszek: wyświetlenie komunikatu i zachowanie stanu do ponownej próby

2. **Walidacja wprowadzanych danych:**
    - Przekroczenie limitu znaków: dezaktywacja dalszego wprowadzania tekstu
    - Puste pola w edycji fiszki: wyświetlenie komunikatu i blokada zapisu

3. **Błędy sieciowe:**
    - Wyświetlenie odpowiedniego komunikatu o braku połączenia
    - Automatyczne ponowienie próby połączenia

4. **Obsługa długich czasów odpowiedzi:**
    - Wyświetlenie wskaźnika postępu dla długich operacji generowania
    - Informacja o szacowanym czasie oczekiwania

## 11. Kroki implementacji

1. **Utworzenie komponentów:**
   ```bash
   ng generate component flashcards/components/generate/text-input --standalone
   ng generate component flashcards/components/generate/action-panel --standalone
   ng generate component flashcards/components/generate/flashcard-grid --standalone
   ng generate component flashcards/components/generate/flashcard-item --standalone
   ```

2. **Implementacja modeli danych:**
    - Utworzenie pliku `flashcards/models/generate.models.ts` z definicjami interfejsów
    - Rozszerzenie modelu `FlashcardViewModel` o pole `isSelected` dla obsługi wyboru fiszek

3. **Implementacja usługi stanu:**
   ```bash
   ng generate service flashcards/services/generate-state
   ```

4. **Implementacja GenerateComponent:**
    - Implementacja logiki zarządzania stanem
    - Implementacja integracji z API
    - Koordynacja podkomponentów

5. **Implementacja TextInputComponent:**
    - Utworzenie pola tekstowego z licznikiem znaków
    - Implementacja walidacji

6. **Implementacja ActionPanelComponent:**
    - Utworzenie przycisków akcji
    - Implementacja emitowanych zdarzeń
    - Implementacja przycisków nawigacji do innych widoków
    - Implementacja przycisków do operacji masowych

7. **Implementacja FlashcardGridComponent:**
    - Utworzenie responsywnej siatki kart
    - Obsługa wyświetlania fiszek
    - Implementacja mechanizmu wyboru fiszek
    - Obsługa trybu wyboru i zaznaczania wielu fiszek

8. **Implementacja FlashcardItemComponent:**
    - Utworzenie widoku pojedynczej fiszki
    - Implementacja akcji (akceptuj, edytuj, odrzuć)
    - Implementacja trybu edycji
    - Dodanie checkboxa do zaznaczania fiszki w trybie wyboru

9. **Integracja komponentów:**
    - Połączenie wszystkich komponentów w GenerateComponent
    - Przekazanie odpowiednich propsów i zdarzeń

10. **Testowanie:**
    - Testy jednostkowe komponentów
    - Testy integracyjne przepływu generowania i zapisywania fiszek

11. **Optymalizacje:**
    - Implementacja wykrywania zmian OnPush dla lepszej wydajności
    - Dodanie mechanizmu buforowania wyników generacji