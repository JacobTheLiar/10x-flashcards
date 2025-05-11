import {Component, Input, OnChanges, SimpleChanges, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, FormGroup, FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {FlashcardApiModel} from '@api/models/flashcard-api-model';

@Component({
  selector: 'app-flashcard-content',
  templateUrl: './flashcard-content.component.html',
  styleUrls: ['./flashcard-content.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule
  ]
})
export class FlashcardContentComponent implements OnChanges {
  @Input() flashcard!: FlashcardApiModel;
  @Input() isEditing = false;
  @Input() form?: FormGroup;

  readonly fb = inject(FormBuilder);

  ngOnChanges(changes: SimpleChanges): void {
    // Jeśli formularz nie został przekazany z zewnątrz, tworzymy go lokalnie
    if (!this.form && this.flashcard && changes['flashcard']) {
      this.initForm();
    }
  }

  /**
   * Inicjalizuje formularz z danymi fiszki i walidacją
   */
  private initForm(): void {
    this.form = this.fb.group({
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

  /**
   * Formatuje datę do polskiego formatu
   */
  formatDate(dateString: string | undefined): string {
    if (!dateString) return '';

    const date = new Date(dateString);
    return new Intl.DateTimeFormat('pl-PL', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  /**
   * Zamienia typ źródła fiszki na przyjazną dla użytkownika nazwę
   */
  getFormattedSourceType(type: string | undefined): string {
    switch (type) {
      case 'ai-full':
        return 'AI';
      case 'ai-edited':
        return 'AI (edytowane)';
      case 'manual':
        return 'Ręcznie';
      default:
        return 'Nieznane źródło';
    }
  }
}
