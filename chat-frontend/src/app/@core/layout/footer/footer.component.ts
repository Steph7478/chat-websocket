import { Component, OnInit, OnDestroy } from '@angular/core';

@Component({
    selector: 'footer',
    standalone: true,
    templateUrl: './footer.component.html',
    styleUrls: ['./footer.component.css'],
})
export class FooterComponent implements OnInit, OnDestroy {
    time = '';
    date = '';
    private intervalId!: number;

    ngOnInit() {
        this.updateClock();
        this.intervalId = window.setInterval(() => {
            this.updateClock();
        }, 1000);
    }

    ngOnDestroy() {
        clearInterval(this.intervalId);
    }

    private updateClock() {
        const now = new Date();

        const h = now.getHours().toString().padStart(2, '0');
        const m = now.getMinutes().toString().padStart(2, '0');

        this.time = `${h}:${m}`;

        this.date = now.toLocaleDateString('pt-BR');
    }
}
