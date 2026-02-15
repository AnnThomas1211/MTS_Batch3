import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth-service';
import { Account } from '../../models/account';
import { AccountService } from '../../service/account-service';
import { AccountStatus } from '../../enums/AccountStatus';

@Component({
  selector: 'app-dashboard-component',
  standalone: false,
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit {

  public account = signal<Account>({
    id: 0,
    holderName: '',
    balance: 0,
    status: AccountStatus.ACTIVE,
    lastUpdated: new Date()
  });

  constructor(
    private accountService: AccountService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAccount();
  }

  loadAccount() {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    // temp substitute accId
    this.accountService.getAccount(2).subscribe((data) => {
      if (data)
        this.account.set(data);
    });
    console.log(this.account().id);
  }

  logout(){
    this.auth.logout();
  }
}
