import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ChatCryptoService {
    private keyPair!: CryptoKeyPair;

    async init() {
        if (!this.keyPair) {
            this.keyPair = await crypto.subtle.generateKey(
                {
                    name: 'RSA-OAEP',
                    modulusLength: 2048,
                    publicExponent: new Uint8Array([1, 0, 1]),
                    hash: 'SHA-256'
                },
                true,
                ['encrypt', 'decrypt']
            );
        }
    }

    async exportPublicKey(): Promise<string> {
        const buf = await crypto.subtle.exportKey('spki', this.keyPair.publicKey);
        return btoa(String.fromCharCode(...new Uint8Array(buf)));
    }

    async encrypt(text: string, pubKeyB64: string): Promise<string> {
        const bin = Uint8Array.from(atob(pubKeyB64), c => c.charCodeAt(0));
        const key = await crypto.subtle.importKey(
            'spki',
            bin,
            { name: 'RSA-OAEP', hash: 'SHA-256' },
            false,
            ['encrypt']
        );

        const enc = await crypto.subtle.encrypt(
            { name: 'RSA-OAEP' },
            key,
            new TextEncoder().encode(text)
        );

        return btoa(String.fromCharCode(...new Uint8Array(enc)));
    }

    async decrypt(encB64: string): Promise<string> {
        const bin = Uint8Array.from(atob(encB64), c => c.charCodeAt(0));
        const dec = await crypto.subtle.decrypt(
            { name: 'RSA-OAEP' },
            this.keyPair.privateKey,
            bin
        );
        return new TextDecoder().decode(dec);
    }
}