import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-generate',
  standalone: true,
  imports: [CommonModule],
  template: `<h2>Generator fiszek - wkrótce dostępny</h2>`,
  styles: [`
    h2 {
      margin: 50px;
      text-align: center;
    }
  `]
})
export class GenerateComponent {
}
