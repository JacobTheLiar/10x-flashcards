import {createAction, props} from '@ngrx/store';
import {GetAllFlashcardsResponse} from '@api/models/get-all-flashcards-response';
import {UpdateFlashcardRequest} from '@api/models/update-flashcard-request';
import {FlashcardApiModel} from '@api/models/flashcard-api-model';

// Load flashcards actions
export const loadFlashcards = createAction(
  '[Flashcards] Load Flashcards'
);

export const loadFlashcardsSuccess = createAction(
  '[Flashcards] Load Flashcards Success',
  props<{ response: GetAllFlashcardsResponse }>()
);

export const loadFlashcardsFailure = createAction(
  '[Flashcards] Load Flashcards Failure',
  props<{ error: string }>()
);

// Update flashcard actions
export const updateFlashcard = createAction(
  '[Flashcards] Update Flashcard',
  props<{ id: string, request: UpdateFlashcardRequest }>()
);

export const updateFlashcardSuccess = createAction(
  '[Flashcards] Update Flashcard Success',
  props<{ flashcard: FlashcardApiModel }>()
);

export const updateFlashcardFailure = createAction(
  '[Flashcards] Update Flashcard Failure',
  props<{ error: string }>()
);

// Delete flashcard actions
export const deleteFlashcard = createAction(
  '[Flashcards] Delete Flashcard',
  props<{ id: string }>()
);

export const deleteFlashcardSuccess = createAction(
  '[Flashcards] Delete Flashcard Success',
  props<{ id: string }>()
);

export const deleteFlashcardFailure = createAction(
  '[Flashcards] Delete Flashcard Failure',
  props<{ error: string }>()
);

// Other actions
export const clearFlashcardsErrors = createAction(
  '[Flashcards] Clear Flashcards Errors'
);

export const toggleEditFlashcard = createAction(
  '[Flashcards] Toggle Edit Flashcard',
  props<{ id: string }>()
);
