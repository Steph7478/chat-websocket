import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../@core/services/auth/auth.service';
import { ModalControlService } from '../../../../shared/logoutModal.service';

@Component({
    selector: 'logout-modal',
    standalone: true,
    templateUrl: './logout.component.html',
    styleUrls: ['./logout.component.css']
})
export class LogoutComponent {
    private auth = inject(AuthService);
    private router = inject(Router);
    public modalService = inject(ModalControlService);

    confirm() {
        this.auth.logout().subscribe({
            next: () => {
                this.modalService.close();

                const audio = new Audio('assets/windows-xp-logout.mp3');
                audio.play().catch(err => {
                    console.warn('Audio failed:', err);
                });

                this.router.navigate(['/auth/login']);
            },
            error: (err) => {
                console.error('Error logout:', err);
            }
        });
    }
}
