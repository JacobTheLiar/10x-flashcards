import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

@Component({
  selector: 'app-empty-state',
  templateUrl: './empty-state.component.html',
  styleUrls: ['./empty-state.component.scss'],
  standalone: true,
  imports: [MatButtonModule, MatIconModule]
})
export class EmptyStateComponent {
  @Input() title = 'Brak danych';
  @Input() message = 'Nie znaleziono żadnych danych.';
  @Input() buttonText = 'Wróć';
  @Input() icon = 'inbox';
  @Output() buttonClick = new EventEmitter<void>();
}
