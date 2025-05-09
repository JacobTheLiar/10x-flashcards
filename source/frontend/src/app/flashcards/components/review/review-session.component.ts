import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-review-session',
  standalone: true,
  imports: [CommonModule],
  template: `<h2>Sesja powtórkowa - wkrótce dostępna</h2>`,
  styles: [`
    h2 {
      margin: 50px;
      text-align: center;
    }
  `]
})
export class ReviewSessionComponent {
}
