import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {GenerateState, FlashcardViewModel} from '../models/generate.models';

@Injectable({
  providedIn: 'root'
})
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

  state$: Observable<GenerateState> = this.state.asObservable();

  updateState(newState: Partial<GenerateState>): void {
    this.state.next({...this.state.value, ...newState});
  }

  get currentState(): GenerateState {
    return this.state.value;
  }

  // Akceptacja wszystkich fiszek
  acceptAllFlashcards(): void {
    const updatedFlashcards = this.state.value.flashcards.map(flashcard => ({
      ...flashcard,
      status: 'accepted' as const
    }));

    this.updateState({flashcards: updatedFlashcards});
  }

  // Akceptacja wybranych fiszek
  acceptSelectedFlashcards(): void {
    const updatedFlashcards = this.state.value.flashcards.map(flashcard => {
      if (flashcard.isSelected) {
        return {
          ...flashcard,
          status: 'accepted' as const,
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
  rejectAllFlashcards(): void {
    const updatedFlashcards = this.state.value.flashcards.map(flashcard => ({
      ...flashcard,
      status: 'rejected' as const
    }));

    this.updateState({flashcards: updatedFlashcards});
  }

  // Przełączanie trybu wyboru
  toggleSelectionMode(): void {
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
  toggleFlashcardSelection(flashcard: FlashcardViewModel, selected: boolean): void {
    const updatedFlashcards = this.state.value.flashcards.map(f => {
      if (f === flashcard) {
        return {
          ...f,
          isSelected: selected
        };
      }
      return f;
    });

    this.updateState({flashcards: updatedFlashcards});
  }

  // Czyszczenie stanu
  clearState(): void {
    this.state.next({
      sourceText: '',
      isGenerating: false,
      error: null,
      generationId: null,
      flashcards: [],
      isSaving: false,
      selectionMode: false
    });
  }
}
