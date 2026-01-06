import { Component } from '@angular/core';
import { ChatComponent } from '../components/chat/chat.component';
import { LogoutComponent } from '../../auth/components/logout/logout.component';

@Component({
  standalone: true,
  selector: 'home-page',
  templateUrl: './home.page.html',
  styleUrls: ['./home.page.css'],
  imports: [ChatComponent, LogoutComponent]
})
export class HomePage {
}
