import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
    standalone: true,
    selector: 'auth-page',
    imports: [RouterOutlet],
    templateUrl: './auth.page.html',
    styleUrls: ['./auth.page.css']
})
export class AuthPage { }
