import { Component, OnInit, signal } from '@angular/core';

@Component({
    selector: 'private-modal',
    templateUrl: 'private.component.html',
    styleUrls: ['private.component.css'],
    standalone: true,
})

export class PrivateComponent {
    showModal = signal(false);
    targetUser = signal('');

    openChatWith(user: string) {
        this.targetUser.set(user);
        this.showModal.set(true);
    }

    toggleModal() {
        this.showModal.update(v => !v);
    }
}