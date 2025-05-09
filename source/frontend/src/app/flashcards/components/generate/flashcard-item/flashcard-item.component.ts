import {Component, EventEmitter, Input, OnInit, Output, ChangeDetectionStrategy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {FlashcardViewModel} from '../../../models/generate.models';
import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations';

@Component({
  selector: 'app-flashcard-item',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule
  ],
  templateUrl: './flashcard-item.component.html',
  styleUrls: ['./flashcard-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('statusChange', [
      state('pending', style({
        borderLeftColor: '#2196f3'
      })),
      state('accepted', style({
        borderLeftColor: '#4caf50'
      })),
      state('rejected', style({
        borderLeftColor: '#f44336',
        opacity: 0.7
      })),
      transition('* => *', animate('200ms ease-in-out'))
    ]),
    trigger('editMode', [
      transition(':enter', [
        style({opacity: 0, transform: 'scale(0.95)'}),
        animate('200ms ease-in-out', style({opacity: 1, transform: 'scale(1)'}))
      ]),
      transition(':leave', [
        animate('200ms ease-in-out', style({opacity: 0, transform: 'scale(0.95)'}))
      ])
    ])
  ]
})
export class FlashcardItemComponent implements OnInit {
  @Input() flashcard!: FlashcardViewModel;
  @Input() isSelectable: boolean = false;
  @Input() isSelected: boolean = false;

  @Output() onAccept = new EventEmitter<FlashcardViewModel>();
  @Output() onReject = new EventEmitter<FlashcardViewModel>();
  @Output() onEdit = new EventEmitter<FlashcardViewModel>();
  @Output() onSelect = new EventEmitter<boolean>();

  editMode: boolean = false;
  editFrontContent: string = '';
  editBackContent: string = '';

  ngOnInit(): void {
    this.resetEditForm();
  }

  resetEditForm(): void {
    this.editFrontContent = this.flashcard.frontContent;
    this.editBackContent = this.flashcard.backContent;
  }

  acceptFlashcard(): void {
    this.onAccept.emit(this.flashcard);
  }

  rejectFlashcard(): void {
    this.onReject.emit(this.flashcard);
  }

  startEditing(): void {
    this.editMode = true;
    this.resetEditForm();
  }

  cancelEditing(): void {
    this.editMode = false;
  }

  saveEdit(): void {
    if (!this.editFrontContent.trim() || !this.editBackContent.trim()) {
      return; // Walidacja - zawartość nie może być pusta
    }

    const editedFlashcard: FlashcardViewModel = {
      ...this.flashcard,
      frontContent: this.editFrontContent.trim(),
      backContent: this.editBackContent.trim(),
      sourceType: 'ai-edited'
    };

    this.onEdit.emit(editedFlashcard);
    this.editMode = false;
  }

  onSelectionChange(selected: boolean): void {
    this.onSelect.emit(selected);
  }

  getCardStatus(): string {
    switch (this.flashcard.status) {
      case 'accepted':
        return 'Zaakceptowano';
      case 'rejected':
        return 'Odrzucono';
      default:
        return 'Oczekuje';
    }
  }

  getCardStatusClass(): string {
    return `status-${this.flashcard.status}`;
  }
}
