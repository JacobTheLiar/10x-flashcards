import {Component, EventEmitter, Input, OnInit, Output, ChangeDetectionStrategy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';

@Component({
  selector: 'app-text-input',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './text-input.component.html',
  styleUrls: ['./text-input.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TextInputComponent implements OnInit {
  @Input() value: string = '';
  @Input() maxLength: number = 10000;
  @Input() placeholder: string = 'Wprowadź tekst do analizy';
  @Input() disabled: boolean = false;

  @Output() textChange = new EventEmitter<string>();

  inputValue: string = '';

  ngOnInit(): void {
    this.inputValue = this.value;
  }

  onTextChange(newValue: string): void {
    // Ograniczenie długości tekstu
    if (newValue.length > this.maxLength) {
      this.inputValue = newValue.substring(0, this.maxLength);
      return;
    }

    this.textChange.emit(newValue);
  }
}
