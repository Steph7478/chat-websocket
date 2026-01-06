import {
    Component,
    ElementRef,
    inject,
    OnDestroy,
    OnInit,
    signal,
    ViewChild
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ChatService } from '../../../../@core/services/chat/chat.service';
import { ChatMessage } from '../../../../@core/api/chat/chat.types';
import { scrollToBottom } from '../../../../shared/scrollToBotton.component';

@Component({
    selector: 'private-modal',
    templateUrl: 'private.component.html',
    styleUrls: ['private.component.css'],
    standalone: true,
})
export class PrivateComponent implements OnInit, OnDestroy {

    @ViewChild('scrollContainer') scrollContainer!: ElementRef;

    showModal = signal(false);
    targetUser = signal('');
    privateMessages = signal<ChatMessage[]>([]);
    messageText = signal('');

    private subs = new Subscription();
    private chat = inject(ChatService);

    ngOnInit() {
        this.subs.add(
            this.chat.messages$.subscribe(msgs => {
                const last = msgs.at(-1);
                if (!last) return;

                if (last.private && last.to === this.chat.currentUser) {
                    if (!this.showModal() || this.targetUser() !== last.from) {
                        this.openChatWith(last.from);
                    }
                }
            })
        );
    }

    openChatWith(user: string) {
        this.targetUser.set(user);
        this.showModal.set(true);

        this.subs.add(
            this.chat.messages$.subscribe(msgs => {
                const me = this.chat.currentUser;

                this.privateMessages.set(
                    msgs.filter(m =>
                        m.private &&
                        (
                            (m.from === user && m.to === me) ||
                            (m.from === me && m.to === user)
                        )
                    )
                );

                scrollToBottom(this.scrollContainer);
            })
        );
    }

    send() {
        const text = this.messageText();
        if (!text.trim()) return;

        this.chat.sendPrivate(this.targetUser(), text);
        this.messageText.set('');
    }

    toggleModal() {
        this.showModal.set(false);
        this.privateMessages.set([]);
        this.targetUser.set('');
    }

    ngOnDestroy() {
        this.subs.unsubscribe();
    }
}
