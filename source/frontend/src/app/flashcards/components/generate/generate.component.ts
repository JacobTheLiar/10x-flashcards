import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {GenerationControllerService} from '@api/services/generation-controller.service';
import {FlashcardControllerService} from '@api/services/flashcard-controller.service';
import {GenerateStateService} from '../../services/generate-state.service';
import {ApiErrorHandlerService} from '../../services/api-error-handler.service';
import {TextInputComponent} from './text-input/text-input.component';
import {ActionPanelComponent} from './action-panel/action-panel.component';
import {FlashcardGridComponent} from './flashcard-grid/flashcard-grid.component';
import {FlashcardViewModel} from '../../models/generate.models';
import {catchError, finalize} from 'rxjs';

@Component({
  selector: 'app-generate',
  standalone: true,
  imports: [
    CommonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    TextInputComponent,
    ActionPanelComponent,
    FlashcardGridComponent,
  ],
  templateUrl: './generate.component.html',
  styleUrls: ['./generate.component.scss']
})
export class GenerateComponent implements OnInit {
  constructor(
    private generationControllerService: GenerationControllerService,
    private flashcardControllerService: FlashcardControllerService,
    public generateState: GenerateStateService,
    private router: Router,
    private snackBar: MatSnackBar,
    private errorHandler: ApiErrorHandlerService
  ) {
  }

  ngOnInit(): void {
    // Czyszczenie stanu przy inicjalizacji komponentu
    this.generateState.clearState();
  }

  // Gettery dla szablonu
  get hasText(): boolean {
    return this.generateState.currentState.sourceText !== '';
  }

  get hasSelectedFlashcards(): boolean {
    return this.generateState.currentState.flashcards.some(f => f.isSelected);
  }

  // Metody dla podkomponentów

  onTextChange(text: string): void {
    this.generateState.updateState({sourceText: text});
  }

  onGenerate(): void {
    const sourceText = this.generateState.currentState.sourceText;

    if (!sourceText?.trim()) {
      this.errorHandler.showError('Wprowadź tekst do analizy');
      return;
    }

    this.generateFlashcards(sourceText);
  }

  onClear(): void {
    this.generateState.clearState();
  }

  onSaveAll(): void {
    this.generateState.acceptAllFlashcards();
    this.saveAcceptedFlashcards();
  }

  onSaveSelected(): void {
    this.generateState.acceptSelectedFlashcards();
    this.saveAcceptedFlashcards();
  }

  onRejectAll(): void {
    this.generateState.rejectAllFlashcards();
  }

  onToggleSelectionMode(): void {
    this.generateState.toggleSelectionMode();
  }

  onNavigateToList(): void {
    this.router.navigate(['/flashcards']);
  }

  onNavigateToReview(): void {
    this.router.navigate(['/review']);
  }

  onAcceptFlashcard(flashcard: FlashcardViewModel): void {
    this.updateFlashcardStatus(flashcard, 'accepted');
  }

  onRejectFlashcard(flashcard: FlashcardViewModel): void {
    this.updateFlashcardStatus(flashcard, 'rejected');
  }

  onEditFlashcard(flashcard: FlashcardViewModel): void {
    const updatedFlashcards = this.generateState.currentState.flashcards.map(f => {
      if (f === flashcard) {
        return {
          ...flashcard,
          status: 'accepted' as const
        };
      }
      return f;
    });

    this.generateState.updateState({flashcards: updatedFlashcards});
  }

  onSelectFlashcard(data: { flashcard: FlashcardViewModel, selected: boolean }): void {
    this.generateState.toggleFlashcardSelection(data.flashcard, data.selected);
  }

  onSelectAll(): void {
    const updatedFlashcards = this.generateState.currentState.flashcards.map(flashcard => ({
      ...flashcard,
      isSelected: true
    }));

    this.generateState.updateState({flashcards: updatedFlashcards});
  }

  onDeselectAll(): void {
    const updatedFlashcards = this.generateState.currentState.flashcards.map(flashcard => ({
      ...flashcard,
      isSelected: false
    }));

    this.generateState.updateState({flashcards: updatedFlashcards});
  }

  // Integracja z API

  private generateFlashcards(sourceText: string): void {
    this.generateState.updateState({isGenerating: true, error: null});

    this.generationControllerService.generateFlashcards({
      body: {sourceText}
    }).pipe(
      finalize(() => this.generateState.updateState({isGenerating: false})),
      catchError(error => {
        this.generateState.updateState({error: 'Nie udało się wygenerować fiszek.'});
        return this.errorHandler.handleError(error, 'Wystąpił błąd podczas generowania fiszek');
      })
    ).subscribe({
      next: (response) => {
        // Sprawdzenie czy response i suggestedFlashcards istnieją
        if (!response || !response.suggestedFlashcards || response.suggestedFlashcards.length === 0) {
          this.generateState.updateState({error: 'Otrzymano pustą odpowiedź z serwera.'});
          this.errorHandler.showError('Nie udało się wygenerować fiszek. Spróbuj ponownie.');
          return;
        }

        const flashcards = response.suggestedFlashcards.map(card => ({
          frontContent: card.frontContent || '',
          backContent: card.backContent || '',
          status: 'pending' as const,
          sourceType: 'ai-full' as const
        }));

        this.generateState.updateState({
          flashcards,
          generationId: response.generationId || ''
        });

        this.errorHandler.showSuccess('Wygenerowano propozycje fiszek');
      }
    });
  }

  private saveAcceptedFlashcards(): void {
    const acceptedFlashcards = this.generateState.currentState.flashcards.filter(f => f.status === 'accepted');

    if (acceptedFlashcards.length === 0) {
      this.errorHandler.showError('Brak zaakceptowanych fiszek do zapisania');
      return;
    }

    const generationId = this.generateState.currentState.generationId;
    if (!generationId) {
      this.errorHandler.showError('Brak identyfikatora generacji. Spróbuj wygenerować fiszki ponownie.');
      return;
    }

    this.generateState.updateState({isSaving: true});

    this.flashcardControllerService.saveFlashcards({
      body: {
        generationId,
        flashcards: acceptedFlashcards.map(card => ({
          frontContent: card.frontContent,
          backContent: card.backContent,
          sourceType: card.sourceType
        }))
      }
    }).pipe(
      finalize(() => this.generateState.updateState({isSaving: false})),
      catchError(error => {
        this.generateState.updateState({error: 'Nie udało się zapisać fiszek.'});
        return this.errorHandler.handleError(error, 'Wystąpił błąd podczas zapisywania fiszek');
      })
    ).subscribe({
      next: (response) => {
        const savedCount = response?.savedCount || 0;
        this.errorHandler.showSuccess(`Zapisano ${savedCount} fiszek`);
        this.generateState.clearState();
      }
    });
  }

  // Pomocnicze metody

  private updateFlashcardStatus(flashcard: FlashcardViewModel, status: 'accepted' | 'rejected'): void {
    const updatedFlashcards = this.generateState.currentState.flashcards.map(f => {
      if (f === flashcard) {
        return {
          ...f,
          status
        };
      }
      return f;
    });

    this.generateState.updateState({flashcards: updatedFlashcards});
  }
}
