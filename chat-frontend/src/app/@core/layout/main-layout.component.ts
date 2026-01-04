import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FooterComponent } from './footer/footer.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, FooterComponent],
  template: `
    <main>
      <router-outlet />
      <footer></footer>
    </main>
  `
})
export class MainLayoutComponent { }