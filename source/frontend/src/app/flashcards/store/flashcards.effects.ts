import {Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {catchError, map, mergeMap, of, tap} from 'rxjs';
import * as FlashcardsActions from './flashcards.actions';
import {FlashcardControllerService} from '@api/services/flashcard-controller.service';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';

@Injectable()
export class FlashcardsEffects {
  constructor(
    private actions$: Actions,
    private flashcardService: FlashcardControllerService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
  }

  // Efekt ładowania fiszek
  loadFlashcards$ = createEffect(() => this.actions$.pipe(
    ofType(FlashcardsActions.loadFlashcards),
    mergeMap(() => this.flashcardService.getAllUserFlashcards().pipe(
      map(response => FlashcardsActions.loadFlashcardsSuccess({response})),
      catchError(error => {
        console.error('Error loading flashcards:', error);
        const errorMessage = error.error?.message || 'Nie udało się załadować fiszek';

        // Obsługa błędów autoryzacji
        if (error.status === 401) {
          this.router.navigate(['/login']);
          return of(FlashcardsActions.loadFlashcardsFailure({error: 'Sesja wygasła. Zaloguj się ponownie.'}));
        }

        return of(FlashcardsActions.loadFlashcardsFailure({error: errorMessage}));
      })
    ))
  ));

  // Efekt aktualizacji fiszki
  updateFlashcard$ = createEffect(() => this.actions$.pipe(
    ofType(FlashcardsActions.updateFlashcard),
    mergeMap(({id, request}) => this.flashcardService.updateFlashcard({
      flashcardId: id,
      body: request
    }).pipe(
      map(flashcard => FlashcardsActions.updateFlashcardSuccess({flashcard})),
      tap(() => {
        this.snackBar.open('Fiszka zaktualizowana pomyślnie', 'OK', {
          duration: 3000
        });
      }),
      catchError(error => {
        console.error('Error updating flashcard:', error);
        const errorMessage = error.error?.message || 'Nie udało się zaktualizować fiszki';

        this.snackBar.open(errorMessage, 'OK', {
          duration: 5000
        });

        // Obsługa błędów autoryzacji
        if (error.status === 401) {
          this.router.navigate(['/login']);
          return of(FlashcardsActions.updateFlashcardFailure({error: 'Sesja wygasła. Zaloguj się ponownie.'}));
        }

        return of(FlashcardsActions.updateFlashcardFailure({error: errorMessage}));
      })
    ))
  ));

  // Efekt usuwania fiszki
  deleteFlashcard$ = createEffect(() => this.actions$.pipe(
    ofType(FlashcardsActions.deleteFlashcard),
    mergeMap(({id}) => this.flashcardService.deleteFlashcard({
      flashcardId: id
    }).pipe(
      map(() => FlashcardsActions.deleteFlashcardSuccess({id})),
      tap(() => {
        this.snackBar.open('Fiszka usunięta pomyślnie', 'OK', {
          duration: 3000
        });
      }),
      catchError(error => {
        console.error('Error deleting flashcard:', error);
        const errorMessage = error.error?.message || 'Nie udało się usunąć fiszki';

        this.snackBar.open(errorMessage, 'OK', {
          duration: 5000
        });

        // Obsługa błędów autoryzacji
        if (error.status === 401) {
          this.router.navigate(['/login']);
          return of(FlashcardsActions.deleteFlashcardFailure({error: 'Sesja wygasła. Zaloguj się ponownie.'}));
        }

        return of(FlashcardsActions.deleteFlashcardFailure({error: errorMessage}));
      })
    ))
  ));
}
