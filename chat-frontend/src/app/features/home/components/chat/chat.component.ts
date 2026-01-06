import {
    Component,
    inject,
    signal,
    computed,
    OnDestroy,
    ViewChild,
    ElementRef
} from '@angular/core';
import { ChatService } from '../../../../@core/services/chat/chat.service';
import { Subscription } from 'rxjs';
import { PrivateComponent } from '../private/private.component';
import { ChatMessage } from '../../../../@core/api/chat/chat.types';
import { scrollToBottom } from '../../../../shared/scrollToBotton.component';

@Component({
    standalone: true,
    selector: 'chat-modal',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.css'],
    imports: [PrivateComponent]
})
export class ChatComponent implements OnDestroy {

    @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

    private chatService = inject(ChatService);

    showModal = signal(false);
    newMessage = signal('');

    users = signal<string[]>([]);

    private allMessages = signal<ChatMessage[]>([]);

    publicMessages = computed(() =>
        this.allMessages().filter(m => !m.private)
    );

    private subs = new Subscription();

    constructor() {
        this.subs.add(
            this.chatService.messages$.subscribe(msgs => {
                this.allMessages.set(msgs);
                scrollToBottom(this.scrollContainer);
            })
        );

        this.subs.add(
            this.chatService.users$.subscribe(users => {
                this.users.set(users);
            })
        );
    }

    toggleModal() {
        this.showModal.update(v => {
            const next = !v;

            next
                ? this.chatService.connect()
                : this.chatService.disconnect();

            return next;
        });
    }

    sendMessage() {
        const text = this.newMessage().trim();
        if (!text) return;

        this.chatService.sendPublic(text);
        this.newMessage.set('');
    }

    ngOnDestroy() {
        this.subs.unsubscribe();
    }
}
