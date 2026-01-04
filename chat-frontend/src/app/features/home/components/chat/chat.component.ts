import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    standalone: true,
    selector: 'chat-modal',
    imports: [CommonModule],
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.css'],
})
export class ChatComponent {
    showModal = signal(false);

    toggleModal() {
        this.showModal.set(!this.showModal());
    }
}
