import {createSelector} from '@ngrx/store';
import {flashcardsAdapter, flashcardsFeature} from './flashcards.reducer';

// Pobierz selektory z entity adaptera i z feature'a
const {
  selectAll,
  selectEntities
} = flashcardsAdapter.getSelectors();

// Selektor wszystkich fiszek jako tablica, posortowanych wg daty modyfikacji
export const selectAllFlashcards = createSelector(
  flashcardsFeature.selectFlashcardsState,
  selectAll
);

// Selektor pojedynczej fiszki po ID
export const selectFlashcardById = (id: string) => createSelector(
  flashcardsFeature.selectEntities,
  (entities) => entities[id]
);

export const selectAllEditingStates = createSelector(
  flashcardsFeature.selectEditingStates,
  (editingStates) => editingStates
);

export const selectIsFlashcardEditing = (id: string) => createSelector(
  flashcardsFeature.selectEditingStates,
  (editingStates) => editingStates[id]?.isEditing || false
);

// Selektor stanu ładowania
export const selectFlashcardsLoading = flashcardsFeature.selectLoading;

// Selektor błędów
export const selectFlashcardsError = flashcardsFeature.selectError;
