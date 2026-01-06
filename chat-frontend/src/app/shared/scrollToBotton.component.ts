import { ElementRef } from '@angular/core';

export function scrollToBottom(
    container?: ElementRef,
    delay = 50
) {
    setTimeout(() => {
        const el = container?.nativeElement;
        if (el) {
            el.scrollTop = el.scrollHeight;
        }
    }, delay);
}
