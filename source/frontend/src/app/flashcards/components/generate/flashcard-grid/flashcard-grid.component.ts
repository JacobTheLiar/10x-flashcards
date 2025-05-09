import {Component, EventEmitter, Input, Output, ChangeDetectionStrategy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {FlashcardViewModel} from '../../../models/generate.models';
import {FlashcardItemComponent} from '../flashcard-item/flashcard-item.component';
import {
  trigger,
  state,
  style,
  animate,
  transition,
  query,
  stagger
} from '@angular/animations';

@Component({
  selector: 'app-flashcard-grid',
  standalone: true,
  imports: [
    CommonModule,
    MatGridListModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    FlashcardItemComponent
  ],
  templateUrl: './flashcard-grid.component.html',
  styleUrls: ['./flashcard-grid.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('fadeAnimation', [
      transition(':enter', [
        style({opacity: 0}),
        animate('300ms', style({opacity: 1}))
      ]),
      transition(':leave', [
        animate('300ms', style({opacity: 0}))
      ])
    ]),
    trigger('cardAnimation', [
      transition(':enter', [
        style({opacity: 0, transform: 'translateY(20px)'}),
        animate('{{delay}}ms ease', style({opacity: 0})),
        animate('300ms ease', style({opacity: 1, transform: 'translateY(0)'}))
      ])
    ]),
    trigger('listAnimation', [
      transition('* => *', [
        query(':enter', [
          style({opacity: 0, transform: 'translateY(20px)'}),
          stagger(100, [
            animate('300ms ease', style({opacity: 1, transform: 'translateY(0)'}))
          ])
        ], {optional: true})
      ])
    ]),
    trigger('countAnimation', [
      transition(':increment', [
        style({transform: 'scale(1.2)'}),
        animate('200ms', style({transform: 'scale(1)'}))
      ]),
      transition(':decrement', [
        style({transform: 'scale(1.2)'}),
        animate('200ms', style({transform: 'scale(1)'}))
      ])
    ])
  ]
})
export class FlashcardGridComponent {
  @Input() flashcards: FlashcardViewModel[] = [];
  @Input() loading: boolean = false;
  @Input() selectionMode: boolean = false;

  @Output() onAccept = new EventEmitter<FlashcardViewModel>();
  @Output() onReject = new EventEmitter<FlashcardViewModel>();
  @Output() onEdit = new EventEmitter<FlashcardViewModel>();
  @Output() onSelect = new EventEmitter<{ flashcard: FlashcardViewModel, selected: boolean }>();
  @Output() onSelectAll = new EventEmitter<void>();
  @Output() onDeselectAll = new EventEmitter<void>();

  get columns(): number {
    // Responsywna siatka - liczba kolumn zależna od szerokości okna
    if (window.innerWidth < 600) {
      return 1;
    } else if (window.innerWidth < 960) {
      return 2;
    } else if (window.innerWidth < 1280) {
      return 3;
    } else {
      return 4;
    }
  }

  get selectedFlashcards(): FlashcardViewModel[] {
    return this.flashcards.filter(f => f.isSelected);
  }

  get allSelected(): boolean {
    return this.flashcards.length > 0 && this.flashcards.every(f => f.isSelected);
  }

  isSelected(flashcard: FlashcardViewModel): boolean {
    return flashcard.isSelected === true;
  }

  acceptFlashcard(flashcard: FlashcardViewModel): void {
    this.onAccept.emit(flashcard);
  }

  rejectFlashcard(flashcard: FlashcardViewModel): void {
    this.onReject.emit(flashcard);
  }

  editFlashcard(flashcard: FlashcardViewModel): void {
    this.onEdit.emit(flashcard);
  }

  selectFlashcard(flashcard: FlashcardViewModel, selected: boolean): void {
    this.onSelect.emit({flashcard, selected});
  }

  toggleSelectAll(checked: boolean): void {
    if (checked) {
      this.onSelectAll.emit();
    } else {
      this.onDeselectAll.emit();
    }
  }

  trackByFlashcard(index: number, flashcard: FlashcardViewModel): string {
    return flashcard.id || index.toString();
  }
}
