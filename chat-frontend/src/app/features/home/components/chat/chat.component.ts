import { Component, inject, signal, computed, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ChatService } from '../../../../@core/services/chat/chat.service';
import { Subscription } from 'rxjs';
import { FormsModule } from '@angular/forms';

@Component({
    standalone: true,
    selector: 'chat-modal',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.css'],
    imports: [FormsModule]
})
export class ChatComponent implements OnDestroy {
    @ViewChild('scrollContainer') private scrollContainer!: ElementRef;
    showModal = signal(true);

    service = inject(ChatService);

    users = signal<string[]>([]);
    newMessage = signal('');
    private allMessages = signal<any[]>([]);

    publicMessages = computed(() =>
        this.allMessages().filter(m => m.private === false)
    );

    private subs = new Subscription();
    constructor() {
        this.subs.add(
            this.service.messages$.subscribe(msgs => {
                this.allMessages.set(msgs);
                this.scrollToBottom();
            })
        );

        this.subs.add(
            this.service.users$.subscribe(userList => {
                this.users.set(userList);
            })
        );
    }

    sendMessage() {
        const text = this.newMessage().trim();
        if (text) {
            this.service.sendPublic(text);
            this.newMessage.set('');
            this.scrollToBottom();
        }
    }

    private scrollToBottom(): void {
        setTimeout(() => {
            if (this.scrollContainer) {
                const el = this.scrollContainer.nativeElement;
                el.scrollTop = el.scrollHeight;
            }
        }, 50);
    }

    toggleModal() {
        this.showModal.set(!this.showModal());
    }

    ngOnDestroy() {
        this.subs.unsubscribe();
    }
}