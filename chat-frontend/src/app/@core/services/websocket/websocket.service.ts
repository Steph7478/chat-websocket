import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class WebsocketService {
    private socket?: WebSocket;

    private messagesSubject = new Subject<any>();
    private connectedSubject = new BehaviorSubject<boolean>(false);

    readonly messages$ = this.messagesSubject.asObservable();
    readonly connected$ = this.connectedSubject.asObservable();

    connect(path: string = '/chat') {
        if (this.socket && this.socket.readyState === WebSocket.OPEN) return;

        const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
        const url = `${protocol}://localhost:8080${path}`;

        this.socket = new WebSocket(url);

        this.socket.onopen = () => {
            console.log('[WS] conectado');
            this.connectedSubject.next(true);
        };

        this.socket.onmessage = (event) => {
            try {
                this.messagesSubject.next(JSON.parse(event.data));
            } catch {
                console.warn('[WS] mensagem inválida', event.data);
            }
        };

        this.socket.onerror = () => {
            console.error('[WS] erro');
        };

        this.socket.onclose = () => {
            console.log('[WS] desconectado');
            this.connectedSubject.next(false);
            this.socket = undefined;
        };
    }

    disconnect() {
        if (!this.socket) return;

        if (
            this.socket.readyState === WebSocket.OPEN ||
            this.socket.readyState === WebSocket.CONNECTING
        ) {
            this.socket.close(1000, 'client_disconnect');
        }

        this.socket = undefined;
        this.connectedSubject.next(false);
    }


    send(data: unknown) {
        if (this.socket?.readyState === WebSocket.OPEN) {
            this.socket.send(JSON.stringify(data));
        }
    }
}
