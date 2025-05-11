import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatCardModule} from '@angular/material/card';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatDialogModule, MatDialog} from '@angular/material/dialog';
import {FlashcardApiModel} from '@api/models/flashcard-api-model';
import {ConfirmDialogComponent} from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import {FlashcardContentComponent} from '../content/flashcard-content.component';
import {FlashcardActionsComponent} from '../actions/flashcard-actions.component';

@Component({
  selector: 'app-flashcard-item',
  templateUrl: './flashcard-item.component.html',
  styleUrls: ['./flashcard-item.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatDialogModule,
    FlashcardContentComponent,
    FlashcardActionsComponent,
    ReactiveFormsModule
  ]
})
export class FlashcardItemComponent implements OnChanges {
  @Input() flashcard!: FlashcardApiModel;
  @Input() isEditing = false;
  @Output() edit = new EventEmitter<void>();
  @Output() save = new EventEmitter<{ frontContent: string, backContent: string }>();
  @Output() delete = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  editForm!: FormGroup;

  private dialog = inject(MatDialog);
  private fb = inject(FormBuilder);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['flashcard'] && this.flashcard) {
      this.initForm();
    }
  }

  onEditClick(): void {
    this.edit.emit();
    this.initForm();
  }

  onCancelClick(): void {
    this.cancel.emit();
  }

  onSaveClick(): void {
    if (this.editForm && this.editForm.valid) {
      this.save.emit({
        frontContent: this.editForm.value.frontContent,
        backContent: this.editForm.value.backContent
      });
    }
  }

  onDeleteClick(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '350px',
      data: {
        title: 'Potwierdzenie usunięcia',
        message: `Czy na pewno chcesz usunąć fiszkę "${this.flashcard.frontContent}"?`,
        confirmText: 'Usuń',
        cancelText: 'Anuluj'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.delete.emit();
      }
    });
  }

  private initForm(): void {
    this.editForm = this.fb.group({
      frontContent: [
        this.flashcard.frontContent,
        [Validators.required, Validators.maxLength(500)]
      ],
      backContent: [
        this.flashcard.backContent,
        [Validators.required, Validators.maxLength(200)]
      ]
    });
  }
}
