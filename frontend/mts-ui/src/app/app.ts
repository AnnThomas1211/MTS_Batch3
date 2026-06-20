import { Component } from '@angular/core';
import { AuthService } from './service/auth-service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css'
})
export class App {
  title = 'mts-ui';

  constructor(public authService: AuthService) {}
}
