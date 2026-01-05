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

@Component({
    standalone: true,
    selector: 'chat-modal',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.css'],
    imports: [PrivateComponent]
})
export class ChatComponent implements OnDestroy {

    @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

    showModal = signal(false);
    newMessage = signal('');
    users = signal<string[]>([]);
    private allMessages = signal<any[]>([]);

    publicMessages = computed(() =>
        this.allMessages().filter(m => m.private === false)
    );

    private subs = new Subscription();

    private chatService = inject(ChatService);

    constructor() {
        this.subs.add(
            this.chatService.messages$.subscribe(msgs => {
                this.allMessages.set(msgs);
                this.scrollToBottom();
            })
        );

        this.subs.add(
            this.chatService.users$.subscribe(users => {
                this.users.set(users);
            })
        );
    }

    toggleModal() {
        const open = !this.showModal();
        this.showModal.set(open);

        if (open) {
            this.chatService.connect();
        } else {
            this.chatService.disconnect();
        }
    }

    sendMessage() {
        const text = this.newMessage().trim();
        if (!text) return;

        this.chatService.sendPublic(text);
        this.newMessage.set('');
    }

    private scrollToBottom() {
        setTimeout(() => {
            const el = this.scrollContainer?.nativeElement;
            if (el) el.scrollTop = el.scrollHeight;
        }, 50);
    }

    ngOnDestroy() {
        this.subs.unsubscribe();
        this.chatService.disconnect();
    }

}
