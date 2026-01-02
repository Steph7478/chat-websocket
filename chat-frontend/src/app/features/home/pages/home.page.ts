import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../components/card/card.component';

@Component({
  standalone: true,
  selector: 'home-page',
  imports: [CommonModule, CardComponent],
  templateUrl: './home.page.html',
})
export class HomePage { }
