import {Component, EventEmitter, Input, OnDestroy, OnInit, Output, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatGridListModule} from '@angular/material/grid-list';
import {FlashcardItemComponent} from '../item/flashcard-item.component';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {Subject, takeUntil} from 'rxjs';
import {FlashcardsFacade} from '../../../store/flashcards.facade';
import {FlashcardApiModel} from "@api/models/flashcard-api-model";
import {UpdateFlashcardRequest} from "@api/models/update-flashcard-request";

@Component({
  selector: 'app-flashcards-grid',
  templateUrl: './flashcards-grid.component.html',
  styleUrls: ['./flashcards-grid.component.scss'],
  standalone: true,
  imports: [CommonModule, MatGridListModule, FlashcardItemComponent]
})
export class FlashcardsGridComponent implements OnInit, OnDestroy {
  @Input() flashcards: FlashcardApiModel[] = [];
  @Output() editFlashcard = new EventEmitter<string>();
  @Output() deleteFlashcard = new EventEmitter<string>();

  columns = 4;
  private destroy$ = new Subject<void>();

  private breakpointObserver = inject(BreakpointObserver);
  facade = inject(FlashcardsFacade);

  ngOnInit(): void {
    this.setUpResponsiveGrid();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onEdit(flashcard: FlashcardApiModel): void {
    if (flashcard.id) {
      this.facade.toggleEditFlashcard(flashcard.id);
    }
  }

  onSave(id: string, data: { frontContent: string, backContent: string }): void {
    const request: UpdateFlashcardRequest = {
      frontContent: data.frontContent,
      backContent: data.backContent
    };

    this.facade.updateFlashcard(id, request);
  }

  onDelete(id: string): void {
    this.deleteFlashcard.emit(id);
  }

  onCancel(id: string): void {
    this.facade.toggleEditFlashcard(id);
  }

  private setUpResponsiveGrid(): void {
    this.breakpointObserver.observe([
      Breakpoints.XSmall,
      Breakpoints.Small,
      Breakpoints.Medium,
      Breakpoints.Large,
      Breakpoints.XLarge
    ]).pipe(
      takeUntil(this.destroy$)
    ).subscribe(result => {
      if (result.breakpoints[Breakpoints.XSmall]) {
        this.columns = 1;
      } else if (result.breakpoints[Breakpoints.Small]) {
        this.columns = 2;
      } else if (result.breakpoints[Breakpoints.Medium]) {
        this.columns = 3;
      } else {
        this.columns = 4;
      }
    });
  }
}
