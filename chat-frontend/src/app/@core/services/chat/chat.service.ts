import { inject, Injectable, signal } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';
import { ChatCryptoService } from './chat-crypto.service';
import { ChatSocket } from '../../api/chat/chat.socket';
import { ChatMessage, ChatMessageDto } from '../../api/chat/chat.types';
import { mapChatMessage } from '../../api/chat/chat.mapper';
import { AuthService } from '../auth/auth.service';

@Injectable({ providedIn: 'root' })
export class ChatService {

    private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
    readonly messages$ = this.messagesSubject.asObservable();

    private usersSubject = new BehaviorSubject<string[]>([]);
    readonly users$ = this.usersSubject.asObservable();

    private readonly myUsername = signal<string | null>(null);

    private publicKeys: Record<string, string> = {};
    private pending: Record<string, string> = {};

    private socket = inject(ChatSocket);
    private crypto = inject(ChatCryptoService);
    private auth = inject(AuthService);

    private socketSub?: Subscription;
    private initialized = false;

    async connect() {
        if (this.initialized) return;
        this.initialized = true;

        const me = this.auth.getUsername();
        if (me) this.myUsername.set(me);

        this.socket.connect();
        await this.crypto.init();

        const myKey = await this.crypto.exportPublicKey();

        // server
        this.socket.send({
            type: 'KEY_EXCHANGE',
            to: 'SYSTEM',
            from: '',
            publicKey: myKey
        });

        this.socketSub = this.socket.messages$.subscribe(dto => {
            if (dto.type === 'TEXT' || dto.type === 'ENCRYPTED_MSG') {
                this.processIncoming(dto);
            } else {
                this.handleSystem(dto);
            }
        });
    }

    disconnect() {
        this.socketSub?.unsubscribe();
        this.socket.disconnect();
        this.initialized = false;
    }

    private async handleSystem(dto: ChatMessageDto) {

        if (dto.type === 'USER_LIST') {
            const list = dto.payload?.trim() ? dto.payload.split(',') : [];
            const me = this.myUsername();
            this.usersSubject.next(me ? list.filter(u => u !== me) : list);
            return;
        }

        if (dto.type === 'PUB_KEY_RESPONSE' && dto.publicKey) {
            this.publicKeys[dto.from] = dto.publicKey;

            const text = this.pending[dto.from];
            if (!text) return;

            const encrypted = await this.crypto.encrypt(text, dto.publicKey);

            // server
            this.socket.send({
                from: '',
                to: dto.from,
                payload: encrypted,
                type: 'ENCRYPTED_MSG'
            });

            delete this.pending[dto.from];

            // ui
            this.pushMessage({
                from: this.myUsername()!,
                to: dto.from,
                text,
                private: true,
                time: this.getCurrentTime()
            });
        }
    }

    private async processIncoming(dto: ChatMessageDto) {

        if (dto.type === 'ENCRYPTED_MSG') {
            try {
                const text = await this.crypto.decrypt(dto.payload!);

                // ui
                this.pushMessage({
                    from: dto.from,
                    to: this.myUsername()!,
                    text,
                    private: true,
                    time: this.getCurrentTime()
                });

            } catch {
                // ui
                this.pushMessage({
                    from: dto.from,
                    to: this.myUsername()!,
                    text: '[Erro de criptografia]',
                    private: true,
                    time: this.getCurrentTime()
                });
            }
        } else {
            this.pushMessage(mapChatMessage(dto));
        }
    }

    sendPublic(text: string) {
        if (!text.trim()) return;

        // server
        this.socket.send({
            from: '',
            to: 'TODOS',
            payload: text,
            type: 'TEXT'
        });

        // ui
        this.pushMessage({
            from: this.myUsername()!,
            to: 'TODOS',
            text,
            private: false,
            time: this.getCurrentTime()
        });
    }

    async sendPrivate(to: string, text: string) {
        if (!text.trim()) return;

        const pubKey = this.publicKeys[to];

        if (!pubKey) {
            this.pending[to] = text;
            this.socket.send({ from: '', to, type: 'GET_PUB_KEY' });
            return;
        }

        const encrypted = await this.crypto.encrypt(text, pubKey);

        // server
        this.socket.send({
            from: '',
            to,
            payload: encrypted,
            type: 'ENCRYPTED_MSG'
        });

        // ui
        this.pushMessage({
            from: this.myUsername()!,
            to,
            text,
            private: true,
            time: this.getCurrentTime()
        });
    }

    private pushMessage(msg: ChatMessage) {
        this.messagesSubject.next([...this.messagesSubject.value, msg]);
    }

    private getCurrentTime(): string {
        return new Date().toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    get currentUser(): string | null {
        return this.myUsername();
    }
}
