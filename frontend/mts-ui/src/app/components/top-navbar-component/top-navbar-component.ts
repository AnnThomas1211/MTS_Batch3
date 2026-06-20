import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AuthService } from '../../service/auth-service';

@Component({
  selector: 'app-top-navbar-component',
  standalone: false,
  templateUrl: './top-navbar-component.html',
  styleUrl: './top-navbar-component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopNavbarComponent {
  constructor(private authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
