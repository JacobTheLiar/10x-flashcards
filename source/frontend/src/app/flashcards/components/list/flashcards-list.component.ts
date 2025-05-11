import {Component, OnInit, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';
import {Router} from '@angular/router';
import {FlashcardsFacade} from '../../store/flashcards.facade';
import {FlashcardsListHeaderComponent} from './header/flashcards-list-header.component';
import {FlashcardsGridComponent} from './grid/flashcards-grid.component';
import {EmptyStateComponent} from '../../../shared/components/empty-state/empty-state.component';
import {ErrorStateComponent} from '../../../shared/components/error-state/error-state.component';

@Component({
  selector: 'app-flashcards-list',
  templateUrl: './flashcards-list.component.html',
  styleUrls: ['./flashcards-list.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    FlashcardsListHeaderComponent,
    FlashcardsGridComponent,
    EmptyStateComponent,
    ErrorStateComponent
  ]
})
export class FlashcardsListComponent implements OnInit {
  facade = inject(FlashcardsFacade);
  private router = inject(Router);

  ngOnInit(): void {
    this.loadFlashcards();
  }

  loadFlashcards(): void {
    this.facade.loadFlashcards();
  }

  toggleEditFlashcard(id: string): void {
    this.facade.toggleEditFlashcard(id);
  }

  deleteFlashcard(id: string): void {
    this.facade.deleteFlashcard(id);
  }

  navigateToGenerate(): void {
    this.router.navigate(['/generate']);
  }
}
