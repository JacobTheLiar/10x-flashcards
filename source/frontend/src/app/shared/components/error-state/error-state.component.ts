import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

@Component({
  selector: 'app-error-state',
  templateUrl: './error-state.component.html',
  styleUrls: ['./error-state.component.scss'],
  standalone: true,
  imports: [MatButtonModule, MatIconModule]
})
export class ErrorStateComponent {
  @Input() error!: string;
  @Input() title = 'Wystąpił błąd';
  @Output() retry = new EventEmitter<void>();
}
