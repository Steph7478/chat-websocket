import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class WebsocketService {
    private socket?: WebSocket;

    private connectedSubject = new BehaviorSubject<boolean>(false);
    connected$ = this.connectedSubject.asObservable();

    connect(path: string = '/chat') {
        if (this.socket) return;

        const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
        const url = `${protocol}://localhost:8080${path}`;

        this.socket = new WebSocket(url);

        this.socket.onopen = () => {
            console.log('🟢 WebSocket conectado');
            this.connectedSubject.next(true);
        };

        this.socket.onerror = () => {
            sessionStorage.removeItem('user_session');
        };

        this.socket.onclose = (event) => {
            console.log('🔴 WebSocket desconectado:', event.reason);
            this.connectedSubject.next(false);
            this.socket = undefined;
        };
    }

    disconnect() {
        this.socket?.close();
        this.socket = undefined;
        this.connectedSubject.next(false);
    }

    send(data: unknown) {
        if (this.socket?.readyState === WebSocket.OPEN) {
            this.socket.send(JSON.stringify(data));
        }
    }

    onMessage<T>(): Observable<T> {
        return new Observable(sub => {
            if (!this.socket) return;

            this.socket.onmessage = (event) => {
                sub.next(JSON.parse(event.data) as T);
            };

            return () => {
                this.socket?.close();
            };
        });
    }
}
