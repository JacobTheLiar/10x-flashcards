import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
  selector: 'app-flashcard-actions',
  templateUrl: './flashcard-actions.component.html',
  styleUrls: ['./flashcard-actions.component.scss'],
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatTooltipModule
  ]
})
export class FlashcardActionsComponent {
  @Input() isEditing = false;
  @Input() canSave = true;

  @Output() edit = new EventEmitter<void>();
  @Output() save = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}
