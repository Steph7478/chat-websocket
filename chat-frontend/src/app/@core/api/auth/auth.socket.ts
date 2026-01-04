import { Injectable } from "@angular/core";
import { WebsocketService } from "../../services/websocket/websocket.service";

@Injectable({ providedIn: 'root' })
export class AuthSocket {
    constructor(private ws: WebsocketService) { }

    connect() {
        this.ws.connect();
    }

    disconnect() {
        this.ws.disconnect();
    }
}
