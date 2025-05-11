import {Injectable, inject} from '@angular/core';
import {Store} from '@ngrx/store';
import {toSignal} from '@angular/core/rxjs-interop';
import * as FlashcardsActions from './flashcards.actions';
import * as FlashcardsSelectors from './flashcards.selectors';
import {UpdateFlashcardRequest} from '@api/models/update-flashcard-request';
import {FlashcardApiModel} from '@api/models/flashcard-api-model';

@Injectable({providedIn: 'root'})
export class FlashcardsFacade {
  private readonly store = inject(Store);

  // Konwersja selektorów na sygnały
  readonly flashcards = toSignal(this.store.select(FlashcardsSelectors.selectAllFlashcards), {initialValue: []});
  readonly loading = toSignal(this.store.select(FlashcardsSelectors.selectFlashcardsLoading), {initialValue: false});
  readonly error = toSignal(this.store.select(FlashcardsSelectors.selectFlashcardsError), {initialValue: null});

  // Metoda do sprawdzenia czy fiszka jest w trybie edycji
  isFlashcardEditing(id: string) {
    return toSignal(this.store.select(FlashcardsSelectors.selectIsFlashcardEditing(id)), {initialValue: false});
  }

  // Metoda do pobrania pojedynczej fiszki po ID
  getFlashcardById(id: string) {
    return toSignal(this.store.select(FlashcardsSelectors.selectFlashcardById(id)), {initialValue: null});
  }

  // Akcje
  loadFlashcards(): void {
    this.store.dispatch(FlashcardsActions.loadFlashcards());
  }

  updateFlashcard(id: string, data: UpdateFlashcardRequest): void {
    this.store.dispatch(FlashcardsActions.updateFlashcard({id, request: data}));
  }

  deleteFlashcard(id: string): void {
    this.store.dispatch(FlashcardsActions.deleteFlashcard({id}));
  }

  toggleEditFlashcard(id: string): void {
    this.store.dispatch(FlashcardsActions.toggleEditFlashcard({id}));
  }

  clearErrors(): void {
    this.store.dispatch(FlashcardsActions.clearFlashcardsErrors());
  }
}
