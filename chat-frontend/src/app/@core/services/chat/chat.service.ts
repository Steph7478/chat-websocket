import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ChatCryptoService } from './chat-crypto.service';
import { ChatSocket } from '../../api/chat/chat.socket';
import { ChatMessage, ChatMessageDto } from '../../api/chat/chat.types';
import { mapChatMessage } from '../../api/chat/chat.mapper';

@Injectable({ providedIn: 'root' })
export class ChatService {
    private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
    messages$ = this.messagesSubject.asObservable();

    private usersSubject = new BehaviorSubject<string[]>([]);
    users$ = this.usersSubject.asObservable();

    private publicKeys: Record<string, string> = {};
    private pending: Record<string, string> = {};

    constructor(
        private socket: ChatSocket,
        private crypto: ChatCryptoService
    ) {
        this.init();
    }

    private async init() {
        this.socket.connect();
        await this.crypto.init();
        const myKey = await this.crypto.exportPublicKey();

        this.socket.send({
            type: 'KEY_EXCHANGE',
            to: 'SYSTEM',
            publicKey: myKey,
            from: ''
        });

        this.socket.messages$.subscribe(dto => {
            if (dto.type === 'TEXT' || dto.type === 'ENCRYPTED_MSG') {
                this.processIncoming(dto);
            } else {
                this.handleSystem(dto);
            }
        });
    }
    private async handleSystem(dto: ChatMessageDto) {
        if (dto.type === 'USER_LIST') {
            const list = dto.payload ? dto.payload.split(',') : [];
            this.usersSubject.next(list);
            return;
        }

        if (dto.type === 'PUB_KEY_RESPONSE' && dto.publicKey) {
            this.publicKeys[dto.from] = dto.publicKey;


            const text = this.pending[dto.from];
            if (text) {
                const encrypted = await this.crypto.encrypt(text, dto.publicKey);

                this.socket.send({
                    from: '',
                    to: dto.from,
                    payload: encrypted,
                    type: 'ENCRYPTED_MSG'
                });

                delete this.pending[dto.from];
                this.pushMessage({ from: 'me', text, private: true, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) });
            }
        }
    }

    private async processIncoming(dto: ChatMessageDto) {
        if (dto.type === 'ENCRYPTED_MSG') {
            try {
                const text = await this.crypto.decrypt(dto.payload!);
                this.pushMessage({ from: dto.from, text, private: true, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) });
            } catch {
                this.pushMessage({
                    from: dto.from,
                    text: '[Erro de criptografia]',
                    private: true,
                    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                });
            }
        } else {
            this.pushMessage(mapChatMessage(dto));
        }
    }


    sendPublic(text: string) {
        this.socket.send({
            from: '',
            to: 'TODOS',
            payload: text,
            type: 'TEXT'
        });

        this.pushMessage({ from: 'me', text, private: false, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) });
    }

    async sendPrivate(to: string, text: string) {
        const pubKey = this.publicKeys[to];

        if (!pubKey) {
            this.pending[to] = text;
            this.socket.send({ from: '', to, type: 'GET_PUB_KEY' });
            return;
        }

        const encrypted = await this.crypto.encrypt(text, pubKey);

        this.socket.send({
            from: '',
            to,
            payload: encrypted,
            type: 'ENCRYPTED_MSG'
        });

        this.pushMessage({ from: 'me', text, private: true, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) });
    }

    disconnect() {
        this.socket.disconnect();
    }

    private pushMessage(msg: ChatMessage) {
        this.messagesSubject.next([...this.messagesSubject.value, msg]);
    }
}
