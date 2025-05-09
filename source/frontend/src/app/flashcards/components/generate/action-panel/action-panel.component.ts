import {Component, EventEmitter, Input, Output, ChangeDetectionStrategy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatTooltipModule} from '@angular/material/tooltip';

@Component({
  selector: 'app-action-panel',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatTooltipModule
  ],
  templateUrl: './action-panel.component.html',
  styleUrls: ['./action-panel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActionPanelComponent {
  @Input() isGenerating: boolean = false;
  @Input() hasText: boolean = false;
  @Input() hasSuggestions: boolean = false;
  @Input() hasSelectedFlashcards: boolean = false;
  @Input() selectionMode: boolean = false;

  @Output() onGenerate = new EventEmitter<void>();
  @Output() onClear = new EventEmitter<void>();
  @Output() onSaveAll = new EventEmitter<void>();
  @Output() onSaveSelected = new EventEmitter<void>();
  @Output() onRejectAll = new EventEmitter<void>();
  @Output() onToggleSelectionMode = new EventEmitter<void>();
  @Output() onNavigateToList = new EventEmitter<void>();
  @Output() onNavigateToReview = new EventEmitter<void>();

  generate(): void {
    this.onGenerate.emit();
  }

  clear(): void {
    this.onClear.emit();
  }

  saveAll(): void {
    this.onSaveAll.emit();
  }

  saveSelected(): void {
    this.onSaveSelected.emit();
  }

  rejectAll(): void {
    this.onRejectAll.emit();
  }

  toggleSelectionMode(): void {
    this.onToggleSelectionMode.emit();
  }

  navigateToList(): void {
    this.onNavigateToList.emit();
  }

  navigateToReview(): void {
    this.onNavigateToReview.emit();
  }
}
