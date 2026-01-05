import { Injectable } from '@angular/core';
import { WebsocketService } from '../../services/websocket/websocket.service';
import { ChatMessageDto } from './chat.types';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatSocket {
    constructor(private ws: WebsocketService) { }

    connect() {
        this.ws.connect('/chat');
    }

    disconnect() {
        this.ws.disconnect();
    }

    get messages$(): Observable<ChatMessageDto> {
        return this.ws.messages$;
    }

    get connected$() {
        return this.ws.connected$;
    }

    send(dto: ChatMessageDto) {
        this.ws.send(dto);
    }
}
