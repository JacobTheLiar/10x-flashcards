import {createFeature, createReducer, on} from '@ngrx/store';
import * as FlashcardsActions from './flashcards.actions';
import {FlashcardApiModel} from '@api/models/flashcard-api-model';
import {EntityState, createEntityAdapter} from '@ngrx/entity';

// Interface dla stanu edycji fiszki
export interface FlashcardEditState {
  isEditing: boolean;
}

// Interface dla stanu fiszek z wykorzystaniem EntityState
export interface FlashcardsState extends EntityState<FlashcardApiModel> {
  loading: boolean;
  error: string | null;
  editingStates: Record<string, FlashcardEditState>;
}

// Entity adapter dla fiszek
export const flashcardsAdapter = createEntityAdapter<FlashcardApiModel>({
  selectId: (flashcard: FlashcardApiModel) => flashcard.id!,
  sortComparer: (a, b) => new Date(b.lastModifiedAt!).getTime() - new Date(a.lastModifiedAt!).getTime()
});

// Stan początkowy
export const initialState: FlashcardsState = flashcardsAdapter.getInitialState({
  loading: false,
  error: null,
  editingStates: {}
});

// Utworzenie feature'a
export const flashcardsFeature = createFeature({
  name: 'flashcards',
  reducer: createReducer(
    initialState,

    // Load flashcards
    on(FlashcardsActions.loadFlashcards, (state) => ({
      ...state,
      loading: true,
      error: null
    })),

    on(FlashcardsActions.loadFlashcardsSuccess, (state, {response}) => {
      return flashcardsAdapter.setAll(response.flashcards || [], {
        ...state,
        loading: false
      });
    }),

    on(FlashcardsActions.loadFlashcardsFailure, (state, {error}) => ({
      ...state,
      loading: false,
      error
    })),

    // Update flashcard
    on(FlashcardsActions.updateFlashcard, (state) => ({
      ...state,
      loading: true,
      error: null
    })),

    on(FlashcardsActions.updateFlashcardSuccess, (state, {flashcard}) => {
      // Po udanej aktualizacji, usuwamy stan edycji dla tej fiszki
      const {[flashcard.id!]: _, ...restEditingStates} = state.editingStates;

      return flashcardsAdapter.updateOne(
        {id: flashcard.id!, changes: flashcard},
        {
          ...state,
          loading: false,
          editingStates: restEditingStates
        }
      );
    }),

    on(FlashcardsActions.updateFlashcardFailure, (state, {error}) => ({
      ...state,
      loading: false,
      error
    })),

    // Delete flashcard
    on(FlashcardsActions.deleteFlashcard, (state) => ({
      ...state,
      loading: true,
      error: null
    })),

    on(FlashcardsActions.deleteFlashcardSuccess, (state, {id}) => {
      // Po udanym usunięciu, usuwamy również stan edycji dla tej fiszki
      const {[id]: _, ...restEditingStates} = state.editingStates;

      return flashcardsAdapter.removeOne(id, {
        ...state,
        loading: false,
        editingStates: restEditingStates
      });
    }),

    on(FlashcardsActions.deleteFlashcardFailure, (state, {error}) => ({
      ...state,
      loading: false,
      error
    })),

    // Toggle edit flashcard
    on(FlashcardsActions.toggleEditFlashcard, (state, {id}) => {
      const currentEditState = state.editingStates[id] || {isEditing: false};

      return {
        ...state,
        editingStates: {
          ...state.editingStates,
          [id]: {
            ...currentEditState,
            isEditing: !currentEditState.isEditing
          }
        }
      };
    }),

    // Clear errors
    on(FlashcardsActions.clearFlashcardsErrors, (state) => ({
      ...state,
      error: null
    }))
  )
});

// Eksport selektorów
export const {
  name,
  reducer,
  selectFlashcardsState,
  selectLoading,
  selectError,
  selectEditingStates,
  selectEntities,
  selectIds
} = flashcardsFeature;
