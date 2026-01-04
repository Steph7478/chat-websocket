import { Component, signal, ViewChild } from '@angular/core';
import { ChatComponent } from '../components/chat/chat.component';

@Component({
  standalone: true,
  selector: 'home-page',
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.css'],
  imports: [ChatComponent]
})
export class HomePage {
}
