import { Injectable } from '@angular/core';
import { WebsocketService } from '../../services/websocket/websocket.service';
import { ChatMessageDto } from './chat.types';

@Injectable({ providedIn: 'root' })
export class ChatSocket {
    constructor(private ws: WebsocketService) { }

    connect() {
        this.ws.connect('/chat');
    }

    disconnect() {
        this.ws.disconnect();
    }

    messages$ = this.ws.onMessage<ChatMessageDto>();

    send(dto: ChatMessageDto) {
        this.ws.send(dto);
    }

}
