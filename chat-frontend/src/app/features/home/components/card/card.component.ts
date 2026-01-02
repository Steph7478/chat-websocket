import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    standalone: true,
    selector: 'app-card',
    imports: [CommonModule],
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.css'],
})
export class CardComponent { }
