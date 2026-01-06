import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ModalControlService {
    logoutOpen = signal(true);

    open() { this.logoutOpen.set(true); }
    close() { this.logoutOpen.set(false); }
}