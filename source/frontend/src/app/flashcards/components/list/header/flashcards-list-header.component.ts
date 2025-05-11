import {Component, EventEmitter, Output} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

@Component({
  selector: 'app-flashcards-list-header',
  templateUrl: './flashcards-list-header.component.html',
  styleUrls: ['./flashcards-list-header.component.scss'],
  standalone: true,
  imports: [MatButtonModule, MatIconModule]
})
export class FlashcardsListHeaderComponent {
  @Output() navigateToGenerate = new EventEmitter<void>();
}
